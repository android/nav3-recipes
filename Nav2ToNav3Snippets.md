# Navigation Compose (Nav2) to Navigation3 - Code snippets

This is a guide for migrating common use cases in Navigation Compose (Nav2) to Navigation3. 
It provides code examples for common use cases implemented using Nav2 APIs and equivalent 
code examples for Nav3. These examples are not necessarily the definitive way you should 
implement your code and should be carefully considered based on your individual requirements. 

These use cases are based on Compose, not Fragments.

## Retrieve a NavController

Nav2:
```
val navController = rememberNavController()
```

Nav3:
```
val backStack = remember { mutableStateListOf<Any>(MyKey) }
```

## Build your navigation graph

Nav2:
```
NavHost(navController, "myGraph", "profile") {
  composable("profile") { /* content */ }
  composable("friends") {...}
}
```

Nav3:
```
// Option 1: Use the entryProvider DSL
NavDisplay(backStack, ...,
  entryProvider = entryProvider(fallback) {
    entry("profile") { /* content */ }
    entry("friends") { /* content */ }
  }
)

// Option 2: Use a when statement
NavDisplay(backStack) {
  when(it) {
    "profile" -> NavEntry("profile") { /* content */ }
    "friends" -> NavEntry("friends") { /* content */ }
    else -> error("Unknown route: $it")

  }
}
```

## Navigate to a destination

Nav2:
```
navController.navigate(Route)
```

Nav3:
```
backStack.add(Key)
```

## Navigate back

Nav2:
```
navController.popBackStack()
```

Nav3:
```
backStack.removeAt(backstack.size - 1)
```

## Navigate back to a particular destination

Nav2:
```
navController.popBackStack(Route2, false)

// if true pop one more
```

Nav3:
```
val index = backstack.lastIndexOf(Route2)
if (index != -1) {
    backstack.removeRange(index + 1, backstack.size)
}
```

## Handle a failed pop back

Nav2:
```
if (!navController.popBackStack()) {
    // Call finish() on your Activity
    finish()
}
```

Nav3:
```
// TODO: Explain this better
// Not really a case.
// If you remove all the items from the back stack `NavDisplay` will crash
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

## Pop up to a destination then navigate

Nav2:
```
navController.navigate(
    route = route,
    navOptions =  navOptions {
        popUpToRoute("root")
    }
)
```

Nav3:
```
val index = backstack.lastIndexOf("root")
if (index != -1) {
    backstack.removeRange(index + 1, backstack.size)
}
backstack.add(route)
```

## Save state when popping up

Nav2:
```
navController.navigate(
    route = route,
    navOptions =  navOptions {
        popUpToRoute("root")
        saveState = true
    }
)
```

Nav3:
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

## Define type safe routes

Nav2:
```
@Serializable
data object Profile

NavHost(...) {
  composable<Profile>(...) { } 
}
```

Nav3:
```
@Serializable
data object Profile

NavDisplay(..., entryProvider = entryProvider {
  entry<Profile> { }
})
```

## Reading and writing navigation arguments with type safe routes

Nav2:
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

navController.navigate(Profile("Some Id"))
```

Nav3:
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

## Navigating to a dialog

Nav2:
```
val navController = rememberNavController()

NavHost(navController, startDestination = Home) {
  composable<Home> { ... }
  dialog<Dialog> { ... }
}

navController.navigate(Dialog)
```

Nav3:
```
val backStack = remember { mutableStateListOf<Any>(Home) }

NavDisplay(backStack, sceneStrategy = DialogSceneStrategy<Any>()) {
  when(it) {
    Home -> { ... }
    Dialog -> NavEntry(it, metadata = DialogSceneStrategy.dialog()) {
      // content for the dialog
    }
  }
}
```

## Navigating to an Activity

Nav2:
```
val navController = rememberNavController()

NavHost(navController, startDestination = Home) {
  composable<Home> { ... }
  activity<SomeActivity> { ... }
}

navController.navigate(SomeActivity)
```

Nav3:
```
val backStack = remember { mutableStateListOf<Any>(Home) }

NavDisplay(backStack) {
  when(it) {
    Home -> NavEntry(it) {...}
    SomeActivity -> NavEntry(it) {
      val context = LocalContext.current
      context.startActivity(Intent(context, SomeActivity::class.java))
    }
  }
}

backStack.add(SomeActivity)
```

## Encapsulation

Nav2:
```
@Serializable
data object Home

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

Nav3:
```
@Serializable
object Home

fun EntryProviderBuilder<Any>.homeDestination() {
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

Nav2:
```
@Serializable data object Title

// Route for nested graph
@Serializable data object Game

// Routes inside nested graph
@Serializable data object Match
@Serializable data object InGame


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

Nav3:
```
@Serializable data object Title

// Routes inside nested graph
@Serializable data object Match
@Serializable data object InGame

val mainBackstack = remember { mutableStateListOf<Any>(Title) }
val gameBackstack = remember { mutableStateListOf<Any>(Match) }

var backstack = mainBackStack

NavDisplay(backStack, ...,
  entryProvider = entryProvider {
    entry<Title> {
      TitleScreen(
        onPlayClicked = { backStack += gameBackStack }
      )
    }
    entry<Match> { 
      MatchScreen(
        onStartGame = { backStack.add(InGame) }
      )
    }
  }
)

```

## Create a custom animation when navigating to and from a specific destination

Nav2:
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

Nav3:
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

## Add shared elements between destinations

Nav2:
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

Nav3:
```
val backStack = remember { mutableStateListOf<Any>(RedBox) }
SharedTransitionLayout {
  NavDisplay(backStack, ...,
    entryProvider = entryProvider {
      entry<RedBox> {
        Box(
          Modifier.sharedBounds(
            rememberSharedContentState("name"),
            LocalNavAnimatedContentScope.current
          )
          .clickable(
            onClick = {
              selectFirst.value = !selectFirst.value
              backstack.add(BlueBox)
            }
          )
          .background(Color.Red)
          .size(100.dp)
        ) {
          Text("start", color = Color.White)
        }
      }
      entry<BlueBox> {
        Box(
          Modifier.offset(180.dp, 180.dp)
            .sharedBounds(
              rememberSharedContentState("name"),
              LocalNavAnimatedContentScope.current
            )
            .clickable(
              onClick = {
                selectFirst.value = !selectFirst.value
                backstack.removeLast()
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

Nav2:
```
override fun finish() {
    super.finish()
    ActivityNavigator.applyPopAnimationsToPendingTransition(this)
}
```

Nav3:
```
override fun finish() {
    super.finish()
    overridePendingTransition(R.anim.popEnterAnim, R.anim.popExitAnim)
}
```

## Deeplink to a destination

Nav2:
```
NavHost(...) {
  composable(..., 
    deepLinks = listOf(navDeepLink { url = "deeplink://mydeeplink" })
  ) {

  }
}

navController.navigate(NavDeepLinkRequest.fromUrl("deeplink://mydeeplink").build())
```

Nav3:
```
// TODO: This isn't really equivalent functionality
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

Nav2:
```
NavHost(...) {
  composable(..., 
    deepLinks = listOf(
      navDeepLink {
        action = "action"
        mimeType = "type"
      }
    )
  ) {

  }
}

navController.navigate(
  NavDeepLinkRequest.fromAction("action").setMimeType("type").build()
)
```
Nav3:
```
// TODO: This isn't really equivalent functionality
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

Nav2:
```
override fun onNewIntent(intent: Intent?) {
  super.onNewIntent(intent)
  navController.handleDeepLink(intent)
}
```

Nav3:
```
see deep link recipe
```

## Conditional navigation

// TODO: No Nav2 code and there's already a recipe for this

Nav3:
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

NavDisplay(
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

## Circular navigation

// TODO: Is it assumed that "a" is already on the stack, that's not immediately clear from the code
// Also, not entirely clear what "circular" means in this context, is it just avoiding duplicate entries on the stack? 

Nav2:
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

Nav3:
```
entry("c") {
  DestinationC(
    onNavigateToA = {
      val index = backstack.lastIndexOf("a")
      if (index != -1) {
          // Pop up to and including "a"
          backstack.removeRange(index, backstack.size)
      }
      backstack.add("a")
    },
  )
}
```

## Reference a destination using NavBackStackEntry

// TODO: Not sure what this is showing me

Nav2:
```
val entry = navController.getBackStackEntry<Key>()

val lifecycle = entry.lifecycle
val viewModel = viewModel(entry)
```

Nav3:
```
// Does not exist in Compose land
// You would use CompositionLocals to get the proper component

val lifecycle = LocalLifecycleOwner.current.lifecycle
val viewModel = viewModel(LocalViewModelStoreOwner.current)
```

## Share UI-related data with ViewModel

// TODO: Code snippets for Nav2 and Nav3 are identical

Nav2:
```
@Composable
fun MyScreen(onNavigate: (Any) -> Unit) {
  Button(onClick = { onNavigate(Profile) } { /* ... */ }
}

```

Nav3:
```
@Composable
fun MyScreen(onNavigate: (Any) -> Unit) {
  Button(onClick = { onNavigate(Profile) } { /* ... */ }
}
```

## Expose events from composable

// TODO: Code snippets for Nav2 and Nav3 are identical

Nav2:
```
@Composable
fun MyScreen(onNavigate: (Any) -> Unit) {
  Button(onClick = { onNavigate(Profile) }) { /* ... */ }
}
```
Nav3:
```
@Composable
fun MyScreen(onNavigate: (Any) -> Unit) {
  Button(onClick = { onNavigate(Profile) }) { /* ... */ }
}
```

## Support multiple back stacks

// TODO: This seems to be the Common Navigation UI recipe, remove? 

Nav2:
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

Nav3:
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
    modifier = Modifier.padding(it),
    entryProvider = entryProvider {
      entry(Profile) { ProfileScreen(...) }
      entry(Friends) { FriendsScreen(...) }
    }
  }
}
```

## Integration with the bottom nav bar

// TODO: There's a lot of code here, the main thing to isolate/highlight is the changes to the logic for determining whether the BottomNavigationItem is selected

Nav2:
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

Nav3:
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
            val index = backStack.lastIndexOf(Profile)
            if (index != -1) {
                backStack.removeRange(index, backStack.size)
            }
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
    modifier = Modifier.padding(it),
    entryProvider = entryProvider {
      entry(Profile) { ProfileScreen(...) }
      entry(Friends) { FriendsScreen(...) }
    }
  }
}
```

## Integration with the top app bar

Nav2:
```
// No guidance for this in Nav 2
```

Nav3:
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
         backStack.last { key -> TOP_LEVEL_ROUTES.any { it.key == key } }::class.simpleName
        )
      }
    )
  }
) {
  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeAt( backStack.size - 1 ) },
    modifier = Modifier.padding(it),
    entryProvider = entryProvider {
      entry(Profile) { ProfileScreen(...) }
      entry(Friends) { FriendsScreen(...) }
    }
  }
}
```

## Testing

// TODO: This would be better as a separate guide explaining how by separating your back stack from Nav3 (e.g. into a `Navigator` class) you can 
// move some of your instrumented navigation tests into unit tests that just verify the back stack state after various
// navigation events (e.g. `NavigatorTest`)

Nav2:
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

Nav3:
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
      NavDisplay(backStack = backStack, entryProvider = entryProvider)
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

Nav2:
```
NavHost(navController, Graph, Profile) {
  composable(Profile) { AndroidFragment<ProfileFragment>() }
}
```

Nav3:
```
NavDisplay(backStack, ...,
  entryProvider = entryProvider { 
    entry(Profile) { AndroidFragment<ProfileFragment>() }
  }
)
```