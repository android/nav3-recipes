# Navigation 2.0 to Navigation3 - Code snippets

This document serves as a guide to transitioning Navigation 2 snippets to Navigation 3. It aims to
provide code examples for the most commonly used Navigation 2 APIs. These examples are meant to serve
as examples and not the source of truth for how you should implement your code, which should be
tailored to your specific use case.

These use cases are based on Compose only implementation, not Fragments.

## Retrieving a NavController

Navigation 2:
```
val navController = rememberNavController()
```

Navigation3:
```
val backstack = remember { mutableStateListOf<Any>(MyKey) }
```

## Building Your Graph

Navigation 2:
```
NavHost(navController, "myGraph", "profile") {
  composeable("profile") { /* content */ }
  composeable("friends") {...}
}
```

Navigation3:
```
// Option 1: entryProvider
NavDisplay(backStack, ...,
  entryProvider(fallback) {
    entry("profile") { /* content */ }
    entry("friends") { /* content */ }
  }
)

// Option 2: When statement
NavDisplay(backStack) {
  when(it) {
    "profile" -> NavEntry("profile") { /* content */ }
    "friends" -> NavEntry("friends") { /* content */ }
    else ->

  }
}
```

## Navigation to a Destination

Navigation 2:
```
navController.navigate(Route)
```

Navigation3:
```
backstack.add(Key)
```

## Pop Back

Navigation 2:
```
navController.popBackStack()
```

Navigation3:
```
backStack.removeAt(backstack.size - 1)
```

## Pop back to a particular destination

Navigation 2:
```
navController.popBackStack(Route2, false)

// if true pop one more
```

Navigation3:
```
backstack.dropLastWhile { it != Route2 }
```

## Handle a failed pop back

Navigation 2:
```
if (!navController.popBackStack()) {
    // Call finish() on your Activity
    finish()
}
```

Navigation3:
```
// Not really a case.
// If you remove all the items from the backstack it will crash
// When there is nothing to pop it will go to the next active callback.
// To ensure that the activity finishes, you could do the following.
val handler = BackHandler(true) {
  // This handler will receive system back if NavDisplay's handler is disabled
  finish()
}

if (backstack.size > 1) {
  backStack.removeAt(backStack.size - 1)
} else {
  finish()
}

NavDisplay(backstack) { }
```

## Pop up to a destination the navigate

Navigation 2:
```
navController.navigate(
    route = route,
    navOptions =  navOptions {
        popUpToRoute("root")
    }
)
```

Navigation3:
```
backstack.dropLastWhile { it != "root" }.add(route)
```

## Save state when popping up

Navigation 2:
```
navController.navigate(
    route = route,
    navOptions =  navOptions {
        popUpToRoute("root")
        saveState = true
    }
)
```

Navigation3:
```
val backStack1 = remember { mutableListOf(Root, Profile) }
val backStack2 = remember { mutableListOf(Root, Friends) }

val backStack = if (condition) {
  backstack1
else {
  backstack2
}

NavDisplay (backStack, ...)
```

## Defining Type Safe Routes

Navigation 2:
```
@Serializable
object Profile

NavHost(...) {
  composable<Profile>(...) { } 
}
```

Navigation3:
```
@Serializable
object Profile

NavDisplay(..., entryProvider = entryProvider {
  entry<Profile> { }
})
```

## Navigation to Type Safe Routes

Navigation 2:
```
@Serializable
data class Profile(id: String = "No Profile")

NavHost(navController,...) {
  composable<Profile>(...) {
    val profile = it.toRoute<Profile>()
    val id = profile.id
    // Do something with the id
  } 
}

navController.navigate(Profile)
```

Navigation3:
```
@Serializable
data class Profile(id: String = "No Profile")

NavDisplay(backStack,..., entryProvider = entryProvider {
  entry<Profile> { profile ->
    val id = profile.id
    // do something with the id
  }
})

backStack.add(Profile("Some Id"))
```

## Navigating to a Dialog

Navigation 2:
```
val navController = rememberNavController()

NavHost(navController, startDestination = Home) {
  composable<Home> { ... }
  dialog<Dialog> { ... }
}

navController.navigate(Dialog)
```

Navigation3:
```
val backStack = remember { mutableStateListOf<Any>(Home) }

NavDisplay(backStack, sceneStrategy = DialogSceneStrategy<Any>()) {
  when(it) {
    Home -> NavEntry(it, metadata = metadata = DialogSceneStrategy.dialog()) {
      // content for the dialog
    }
  }
}
```

## Navigating to an Activity

Navigation 2:
```
val navController = rememberNavController()

NavHost(navController, startDestination = Home) {
  composable<Home> { ... }
  activity<SomeActivity> { ... }
}

navController.navigate(SomeActivity)
```

Navigation3:
```
val backStack = remember { mutableStateListOf<Any>(Home) }

NavDisplay(backStack) {
  when(it) {
    Home -> NavEntry(it) {...}
    SomeActivity -> NavEntry(it) {
      startActivity(Intent().setClass(context, SomeActivity::class)
    }
  }
}
```

## Encapsulation

Navigation 2:
```
@Serializable
object Home

fun NavGraphBuilder.homeDestination() {
    composable<Home> { HomeScreen( /* ... */ ) }
}

// MyApp.kt

@Composable
fun MyApp() {
  ...
  NavHost(navController, startDestination = Contacts) {
     homeDestination()
  }
}
```

Navigation3:
```
@Serializable
object Home

fun EntryProviderBuilder.homeDestination() {
    entry<Home> { HomeScreen( /* ... */ ) }
}

// MyApp.kt

@Composable
fun MyApp() {
  ...
  NavDisplay(backStack, entryProvider = entryProvider {
    homeDestination()
  })
}
```

## Nested Navigation

Navigation 2:
```
@Serializable object Title

// Route for nested graph
@Serializable object Game

// Routes inside nested graph
@Serializable object Match
@Serializable object InGame


NavHost(navController, startDestination = Title) {
   composable<Title> {
       TitleScreen(
           onPlayClicked = { navController.navigate(route = Game) }
       )
   }
   navigation<Game>(startDestination = Match) {
       composable<Match> {
           MatchScreen(
               onStartGame = { navController.navigate(route = InGame) }
           )
       }
   }
}
```

Navigation3:
```
@Serializable object Title

// Routes inside nested graph
@Serializable object Match

val mainBackstack = remember { mutableStateListOf<Any>(Title) }
val gameBackstack = remember { mutableStateListOf<Any>(Match) }

var backstack = mainBackStack

NavDisplay(backStack, ...,
  entryProvider() {
    entry(Title) {
      TitleScreen(
        onPlayClicked = { backStack += gameBackStack }
      )
    }
    entry(Game) { 
      MatchScreen(
        onStartGame = { backStack.add(InGame) }
      )
    }
  }
)

```

## Animate between destinations

Navigation 2:
```
val navController = rememberNavController()

NavHost(navController, startDestination = Home) {
  composable<Home> { ... }
  composable<Profile>(
    enterTransition = { /* my custom transition */ }
    exitTransition = { /* my custom transition */ }
  ) {...}
}

navController.navigate(Profile)
```

Navigation3:
```
val backStack = rememberMutableBackStackOf<Any>(Home)

NavDisplay(backstack) {
  when(it) {
    is Home -> NavEntry(it) { ... }
    is Profile ->
      NavEntry(it, transition(/* enterTransition */, /* exitTransition*/)) {
      }
    else ->
  }
}

backStack.add(Profile)
```

## Add sharedElements between destinations

Navigation 2:
```
SharedTransitionLayout {
  val selectFirst = mutableStateOf(true)
  NavHost(navController, startDestination = RedBox) {
    composable<RedBox> {
      Box(
        Modifier.sharedBounds(
          rememberSharedContentState("name"),
          this
        )
        .clickable(
          onClick = {
            selectFirst.value = !selectFirst.value
            navController.navigate(BlueBox)
          }
        )
        .background(Color.Red)
        .size(100.dp)
      ) {
        Text("start", color = Color.White)
      }
    }
    composable<BlueBox> {
      Box(
        Modifier.offset(180.dp, 180.dp)
          .sharedBounds(
            rememberSharedContentState("name"),
            this
          )
          .clickable(
            onClick = {
              selectFirst.value = !selectFirst.value
              navController.popBackStack()
            }
          )
          .alpha(0.5f)
          .background(Color.Blue)
          .size(180.dp)
      ) {
        Text("finish", color = Color.White)
      }
    }
  }
}
```

Navigation3:
```
val backStack = remember { mutableStateListOf<Any>(RedBox) }
SharedTransitionLayout {
  NavDisplay(backStack, ...,
    entryProvider() {
      entry(RedBox) {
        Box(
          Modifier.sharedBounds(
            rememberSharedContentState("name"),
            LocalNavAnimatedContentScope.current
          )
          .clickable(
            onClick = {
              selectFirst.value = !selectFirst.value
              navController.navigate(BlueBox)
            }
          )
          .background(Color.Red)
          .size(100.dp)
        ) {
          Text("start", color = Color.White)
        }
      }
      entry(BlueBox) {
        Box(
          Modifier.offset(180.dp, 180.dp)
            .sharedBounds(
              rememberSharedContentState("name"),
              LocalNavAnimatedContentScope.current
            )
            .clickable(
              onClick = {
                selectFirst.value = !selectFirst.value
                navController.popBackStack()
              }
            )
            .alpha(0.5f)
            .background(Color.Blue)
            .size(180.dp)
        ) {
          Text("finish", color = Color.White)
        }
      }
    }
  )
}
```

## Apply pop animations to activity transitions

Navigation 2:
```
override fun finish() {
    super.finish()
    ActivityNavigator.applyPopAnimationsToPendingTransition(this)
}
```

Navigation3:
```
override fun finish() {
    super.finish()
    overridePendingTransition(R.anim.popEnterAnim, R.anim.popExitAnim)
}
```

## Deeplinking to a destination

Navigation 2:
```
NavHost(...) {
  composable(..., 
    deepLinks = listOf(navDeepLink { url = "deeplink://mydeeplink" })
  ) {

  }
}

navController(NavDeepLinkRequest.fromUrl("deeplink://mydeeplink").build())
```

Navigation3:
```
// Do not deep link internally to destination, just go to them.

val backStack = remember { mutableListOf(MyKey)}

NavDisplay(...) {
 when(it) {
   MyKey -> NavEntry(MyKey) { }
   MyKey2 -> NavEntry(MyKey2) { }
 }
}

backStack.add(MyKey2)
```

## Navigate with actions and mimetypes

Navigation 2:
```
NavHost(...) {
  composable(..., 
    deepLinks = listOf(navDeepLink { action = "action" mimeType = "type" })
  ) {

  }
}

navController(
  NavDeepLinkRequest.fromAction("action").setMimeType("type").build()
)
```
Navigation3:
```
// Do not deep link internally to destination, just go to them.

val backStack = remember { mutableListOf(MyKey)}

NavDisplay(...) {
 when(it) {
   MyKey -> NavEntry(MyKey) { }
   MyKey2 -> NavEntry(MyKey2) { }
 }
}

backStack.add(MyKey2)
```

## Handling deep link from Intent

Navigation 2:
```
override fun onNewIntent(intent: Intent?) {
  super.onNewIntent(intent)
  navController.handleDeepLink(intent)
}
```

Navigation3:
```
val intent = Intent()
intent.data = parseBackStackToUri(backStack)

interface Destination {
  val deepLink: Uri? = null
  val parent: Destination? = null
}

data class Profile(id: String? = null) : Destination {
  override val deepLink = "https://www.example.com/profile/$id"
  override val parent = ProfileList
}

object ProfileList : Destination {
  override val parent = Root
}

object Root : Destination

// From another API where you only have the data
override fun onNewIntent(intent: Intent?) {
  // super.onNewIntent(intent)
  val uri = intent.data
  backStack = changeUriToBackStack(uri)
}



fun parseBackStackToUri(backStack: List<Any>): Uri? {
  return backStack.last().deepLink
}

fun changeUriToBackStack(uri: URI): List<Any> {
  // find every instance of Destination
  val match = destinationList.takeIf {
    uri == it.deepLink
  }
  // find a back stack for the match
  if (match != null) {
    val backStack = mutableListOf(match)
    // Add in a synthetic back stack
    while (backStack.first().parent != null) {
      backStack = backStack.first().parent + backStack
    }
    return backStack
  }
  return listOf()
}

// You own the intent and pull it out
val intent = Intent()
intent.extras = encodeToSavedState(backStack)

override fun onNewIntent(intent: Intent?) {
  // super.onNewIntent(intent)
  backStack = decodeFromSavedState(intent.extras!!)
}
```

## Conditional Navigation

Navigation3:
```
var isLoggedIn by remember { mutableStateOf(false) }
val backStack = remember { mutableStateListOf<Any>(Home) }
val loginStack = remember { mutableStateListOf<Any>(Login) }

val navBackStack =
  if (isLoggedIn) {
    backStack
  } else {
    backStack + loginStack
  }

*NavDisplay(
  backStack = navBackStack,
  modifier = Modifier.padding(paddingValues),
  onBack = { backStack.removeLastOrNull() },
  entryProvider = entryProvider() {
    entry(Home) {
      Column {
        Text("Welcome to Nav3")
        Button(onClick = {
          backStack.add(Product("123"))
        }) {
          Text("Click to add Product")
        }
      }
    }
    entry(Product) {
      Text("Product ${key.id} ")
    }
    entry(Login) {
      Column {
        Text("Login screen")
        Button(onClick = { isLoggedIn = !isLoggedIn }) {
          Text(if (isLoggedIn) { "Logout" } else { "Login" })
        }
        Button(onClick = { backStack.add(Product("ABC"))}) {
          Text("Go to product")
        }
      }
    }
  }
)
```

## Circular Navigation

Navigation 2:
```
composable("c") {
  DestinationC(
    onNavigateToA = {
      navController.navigate("a") {
        popUpTo("a") {
          inclusive = true
        }
      }
    },
  )
}
```

Navigation3:
```
entry("c") {
  DestinationC(
    onNavigateToA = {
      backstack.dropLastWhile { it != "a"}
      backstack.removeLast()
      backstack.add("a")
    },
  )
}
```

## Reference a destination using NavBackStackEntry

Navigation 2:
```
val entry = navController.getBackStackEntry<Key>()

val lifecycle = entry.lifecycle
val viewModel = viewModel(entry)
```

Navigation3:
```
// Does not exist in Compose land
// You would use CompositionLocals to get the proper component

val lifecycle = LocalLifecycleOwner.current.lifecycle
val viewModel = viewModel(LocalViewModelStoreOwner.current)
```

## Share UI-related data with ViewModel

Navigation 2:
```
@Composable
fun MyScreen(onNavigate: (Any) -> Unit) {
  Button(onClick = { onNavigate(Profile) } { /* ... */ }
}

```

Navigation3:
```
@Composable
fun MyScreen(onNavigate: (Any) -> Unit) {
  Button(onClick = { onNavigate(Profile) } { /* ... */ }
}
```

## Expose events from composable

Navigation 2:
```
@Composable
fun MyScreen(onNavigate: (Any) -> Unit) {
  Button(onClick = { onNavigate(Profile) } { /* ... */ }
}
```
Navigation3:
```
@Composable
fun MyScreen(onNavigate: (Any) -> Unit) {
  Button(onClick = { onNavigate(Profile) } { /* ... */ }
}
```

## Support multiple back stacks

Navigation 2:
```
val navController = rememberNavController()
Scaffold(
  bottomBar = {
    BottomNavigation {
      val navBackStackEntry by navController.currentBackStackEntryAsState()
      val currentDestination = navBackStackEntry?.destination
      topLevelRoutes.forEach { topLevelRoute ->
        BottomNavigationItem(
          icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
          label = { Text(topLevelRoute.name) },
          selected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
          onClick = {
            navController.navigate(topLevelRoute.route) {
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          }
        )
      }
    }
  }
) { innerPadding ->
  NavHost(navController, startDestination = Profile, Modifier.padding(innerPadding)) {
    composable<Profile> { ProfileScreen(...) }
    composable<Friends> { FriendsScreen(...) }
  }
}
```

Navigation3:
```
data class TopLevelStack(val stack: MutableList<Any>, val icon: ImageVector)

val profileStack = remember { mutableStateListOf<Any>(Profile) }
val friendsStack = remember { mutableStateListOf<Any>(Friends) }

val TOP_LEVEL_STACKS = listOf(
    TopLevelStack(stack = profileStack, icon = Icons.Profile),
    TopLevelStack(stack = friendsStack, icon = Icons.Friends),
)

var backstack = profileStack

Scaffold(
  bottomBar = {
    NavigationBar {
      TOP_LEVEL_STACKS.forEach { topLevelStack ->
        NavigationBarItem(
          selected = topLevelStack.stack.last() == backStack.last(),
          onClick = {
            backStack.popUntil(Profile)
            if (backStack.last() != topLevelStack.stack.last()) {
              backStack += topLevelStack.stack
            }
          },
          icon = {
            Icon(
              imageVector = topLevelStack.icon,
              contentDescription = topLevelStack.stack.last()::class.simpleName
            )
          }
        )
      }
    }
  }
) {
  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeAt( backStack.size - 1 ) },
    modifier = Modifier.padding(it)
    entryProvider {
      entry(Profile) { ProfileScreen(...) }
      entry(Friends) { FriendsScreen(...) }
    }
  }
}
```

## Integration with the bottom nav bar

Navigation 2:
```
data class TopLevelRoute<T : Any>(val name: String, val route: T, val icon: ImageVector)


val topLevelRoutes = listOf(
   TopLevelRoute("Profile", Profile, Icons.Profile),
   TopLevelRoute("Friends", Friends, Icons.Friends)
)


val navController = rememberNavController()
Scaffold(
  bottomBar = {
    BottomNavigation {
      val navBackStackEntry by navController.currentBackStackEntryAsState()
      val currentDestination = navBackStackEntry?.destination
      topLevelRoutes.forEach { topLevelRoute ->
        BottomNavigationItem(
          icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
          label = { Text(topLevelRoute.name) },
          selected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
          onClick = {
            navController.navigate(topLevelRoute.route) {
              // Pop up to the start destination of the graph to
              // avoid building up a large stack of destinations
              // on the back stack as users select items
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              // Avoid multiple copies of the same destination when
              // reselecting the same item
              launchSingleTop = true
              // Restore state when reselecting a previously selected item
              restoreState = true
            }
          }
        )
      }
    }
  }
) { innerPadding ->
  NavHost(navController, startDestination = Profile, Modifier.padding(innerPadding)) {
    composable<Profile> { ProfileScreen(...) }
    composable<Friends> { FriendsScreen(...) }
  }
}
```

Navigation3:
```
data class TopLevelRoute(val key: Any, val icon: ImageVector)

val TOP_LEVEL_ROUTES = listOf(
    TopLevelRoute(key = Profile, icon = Icons.Profile),
    TopLevelRoute(key = Friends, icon = Icons.Friends),
)

val backStack = remember { mutableStateListOf<Any>(Profile) }

Scaffold(
  bottomBar = {
    NavigationBar {
      TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
        NavigationBarItem(
          selected = topLevelRoute.key == backStack.last(),
          onClick = {
            backStack.popUntil(Home)
            if (backStack.last() != topLevelRoute.key) {
              backStack.add(topLevelRoute.key)
            }
          },
          icon = {
            Icon(
              imageVector = topLevelRoute.icon,
              contentDescription = topLevelRoute.key::class.simpleName
            )
          }
        )
      }
    }
  }
) {
  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeAt( backStack.size - 1 ) },
    modifier = Modifier.padding(it)
    entryProvider {
      entry(Profile) { ProfileScreen(...) }
      entry(Friends) { FriendsScreen(...) }
    }
  }
}
```

## Integration with the top app bar

Navigation 2:
```
// No guidance for this in Nav 2
```

Navigation3:
```
val TOP_LEVEL_ROUTES = listOf(
    TopLevelRoute(key = Profile, icon = Icons.Profile),
    TopLevelRoute(key = Friends, icon = Icons.Friends),
)

val backStack = remember { mutableStateListOf<Any>(Profile) }

Scaffold(
  topBar = {
    TopAppBar(
      title = {
        Text(
         backstack.last { TOP_LEVEL_ROUTES.contains(it) }::class.simpleName
        )
      }
    )
  }
) {
  *NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeAt( backStack.size - 1 ) },
    modifier = Modifier.padding(it)
    entryProvider {
      entry(Profile) { ProfileScreen(...) }
      entry(Friends) { FriendsScreen(...) }
    }
  }
}
```

## Testing

Navigation 2:
```
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    lateinit var navController: TestNavHostController

    @Before
    fun setupAppNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            AppNavHost(navController = navController)
        }
    }

    // Unit test
    @Test
    fun appNavHost_verifyStartDestination() {
        composeTestRule
            .onNodeWithContentDescription("Start Screen")
            .assertIsDisplayed()
    }
}
```

Navigation3:
```
class NavigationTest {

  @get:Rule
  val composeTestRule = createComposeRule()
  lateinit var backStack: MutableList<Any>
  val entryProvider = entryProvider() { ... }

  @Before
  fun setupAppNavHost() {
    composeTestRule.setContent {
      backStack = rememberMutableStateListOf(Start)
      TestDisplay(backStack = backStack, entryProvider = entryProvider)
    }
  }

  // Unit test
  @Test
  fun testDisplay_verifyStartDestination() {
    composeTestRule
      .onNodeWithContentDescription("Start Screen")
      .assertIsDisplayed()
  }
}
```

## Interoperability

Navigation 2:
```
NavHost(navController, Graph, Profile) {
  composeable(Profile) { AndroidFragment<ProfileFragment>() }
}
```

Navigation3:
```
NavDisplay(backStack, ...,
  entryProvider() {
    entry(Profile) { AndroidFragment<ProfileFragment>() }
  }
)
```