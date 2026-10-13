# Responsive Navigation Scene Decorator: Behavior & Constraints

This document describes the architectural requirements, behavioral constraints, and edge-case guarantees provided by the `ResponsiveNavigationSceneDecorator` implementation in Navigation 3.

---

## 1. Movable Content Ownership & Continuity (Single-Caller Invariant)

`movableContentOf` requires that **at most one caller** in the Compose hierarchy calls the lambda on any given frame. Calling it simultaneously from multiple composition sites causes a runtime exception, while calling it from zero sites causes the content (and its internal state/nodes) to detach.

### The Caller Rule

```kotlin
val isMovableContentCaller =
    animatedContentScope.transition.targetState == EnterExitState.Visible ||
        !currentSceneIsDecorated
```

### Transition Matrix

| Scenario | Active / Current Scene | Incoming / Departing Scene | Behavior |
| :--- | :--- | :--- | :--- |
| **Decorated $A \to$ Decorated $B$** (Navigate or Pop) | $A$: `PostExit`, `currentSceneIsDecorated = true` $\implies$ `false` | $B$: `Visible` $\implies$ `true` | Exactly one caller ($B$). Content moves seamlessly without re-creation. |
| **Decorated $A \to$ Undecorated $B$** (e.g. Full-screen) | $A$: `PostExit`, `currentSceneIsDecorated = false` $\implies$ `true` | $B$: Not decorated | $A$ maintains ownership during its exit animation; the bar animates out smoothly with $A$ instead of vanishing on frame 0. |
| **Predictive Back: Decorated $B \to$ Decorated $A$** | $B$: `Visible`, `currentSceneIsDecorated = true` $\implies$ `true` | $A$: `PreEnter`, `currentSceneIsDecorated = true` $\implies$ `false` | $B$ retains caller ownership and renders the bar during the swipe gesture. |
| **Predictive Back: Undecorated $B \to$ Decorated $A$** | $B$: Undecorated (`currentSceneIsDecorated = false`) | $A$: `PreEnter`, `currentSceneIsDecorated = false` $\implies$ `true` | $A$ assumes caller ownership immediately during `defer()`, allowing the bar to display beneath $B$ throughout the gesture rather than popping in upon release. |

---

## 2. Zero-Lag Current Scene Resolution

To avoid a 1-frame lag or flicker during route transitions:
- `rememberSceneState`, `rememberNavigationEventState`, and `NavigationBackHandler` are hoisted at the `NavDisplay` call site.
- `sceneState.currentScene` is inspected synchronously during composition:
  ```kotlin
  val currentSceneIsDecorated = sceneState.currentScene is DecoratedResponsiveScene<*>
  CompositionLocalProvider(LocalCurrentSceneIsDecorated provides currentSceneIsDecorated) {
      NavDisplay(
          sceneState = sceneState,
          navigationEventState = navigationEventState,
      )
  }
  ```
- Decorated scenes read `LocalCurrentSceneIsDecorated.current` in the exact same frame without side-effects (`LaunchedEffect`, `DisposableEffect`) or asynchronous state delays.

---

## 3. Shared Element Anchoring in Deferred Transitions

- **Stationary Scale/Position:** Navigation chrome uses `permitTransformDuringDeferredTransition = false` via `SharedContentConfig`. During predictive back gestures (`defer()`), the shared navigation bar/rail stays stationary at 1.0× scale at the screen edge rather than warping or shrinking with the animating scene card.
- **Overlay Layering:** Navigation slots specify `zIndexInOverlay = 1f` so the chrome renders above scene transitions in the shared element overlay.

---

## 4. Slot Measurement & Lookahead Cache Purity

- **Slot Size Caching:** When a scene is not the movable content caller (`!isMovableContentCaller`), measuring the slot empty (`0 x 0`) would corrupt layout bounds. Strategy-scoped `NavSlotSizeCache` retains the last non-zero measured dimensions and measures with fixed constraints (`Constraints.fixed(cachedWidth, cachedHeight)`).
- **Lookahead Pass Isolation:** Size caches are updated strictly during the lookahead pass (`if (isLookingAhead)`). Intermediate approach or animation passes never pollute the cached target size.
- **Single Measure Invariant:** Each measurable (`nav_chrome` and `scene_content`) is measured exactly once per layout pass, avoiding double-measure runtime crashes.

---

## 5. Inset Consumption & Measure-Time Padding

- **Zero-Frame-Lag Insets:** Navigation chrome dimensions are published via `LocalNavInsets` (a `staticCompositionLocalOf<WindowInsets>`) backed by `MutableWindowInsets` (`chromeInsets`), updated during the measure pass before `scene_content` is measured.
- **Lazy Measure-Phase Reading:** Screen content consumes insets using:
  ```kotlin
  LazyColumn(
      contentPadding = WindowInsets.safeDrawing.union(LocalNavInsets.current).asPaddingValues()
  )
  ```
  Both `union` and `asPaddingValues()` evaluate at measure time, ensuring immediate alignment on the first frame without seed padding or extra recomposition cycles.
- **Max vs. Sum Semantics:** Using `WindowInsets.union(...)` takes the maximum inset per edge instead of summing, preventing double-padding with system cutouts, status bars, and navigation rails.
- **Allocation-Free Inset Guard:** `chromeInsets.insets` is only updated when `LastInsets.changed(left, right, bottom)` returns true, using a plain remembered holder with primitive `Int` fields rather than Compose snapshot state. This prevents measuring from reading and writing snapshot state (which would trigger redundant remeasures) and avoids allocating unnecessary `WindowInsets` instances.

---

## 6. Equality, State Preservation & Metadata Precedence

- **Scene Equality:** `DecoratedResponsiveScene` implements `equals` and `hashCode` based on `scene`, ensuring `rememberSceneState` correctly preserves entry state across recompositions.
- **Opt-In / Opt-Out Precedence:** In multi-pane scenarios (e.g. List-Detail):
  1. Scene metadata (`scene.metadata[NavSceneDecoratorKey]`) takes highest precedence.
  2. If unset, the top/active pane entry metadata (`scene.entries.lastOrNull { it.metadata[...] != null }`) wins.
  This allows detail panes to request full-screen mode even when the base list pane specifies standard navigation.

---

## 7. Hierarchical vs. Top-Level Transitions

`NavDisplay` uses relationship-based transitions between scenes:
- **Same Hierarchy (Material Shared Axis X):** When navigating forward or back within the same top-level destination hierarchy (e.g., `RouteB` $\leftrightarrow$ `RouteB1`), transitions use horizontal slide + fade:
  - Forward: `materialSharedAxisXForward()` (incoming slides in from right, outgoing slides out to left).
  - Pop: `materialSharedAxisXPop()` (incoming slides in from left, outgoing slides out to right).
- **Cross-Destination (Material Fade Through / Crossfade):** When switching between unrelated top-level destinations (e.g., `RouteA` $\leftrightarrow$ `RouteB`), transitions use `materialTopLevelCrossfade()`.
- **Top-Level Root Inference:** Hierarchy ownership is determined via `Scene.activeTopLevelContentKey(topLevelContentKeys)` using `(previousEntries + entries)` to match against the root destination's `contentKey`. Switching back to the start tab (e.g., `RouteB` / `RouteB1` $\to$ `RouteA`) is classified as a pop by `NavDisplay` (`isPop = true`), but because `isSameHierarchy` evaluates to `false`, it cleanly resolves to `materialTopLevelCrossfade()`.

