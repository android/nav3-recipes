# Navigation 3 (Nav3) Frequently Asked Questions

## General Philosophy & Architecture

### Why does Nav3 seem basic compared to Nav2, relying mostly on recipes?
The "basic" nature of Nav3 is **intentional**. While Nav2 provided a pre-packaged "box" of navigation components (like `NavController` and `NavHost`) that were difficult to replace, Nav3 focuses on providing flexible **building blocks** designed to be as "Compose-like" as possible.

This approach allows developers to:
*   Choose exactly which features (like decorators or `NavDisplay`) to include.
*   Use **recipes** as guides to implement common, but nuanced, use cases.
*   Upvote specific recipes; popular recipes may be promoted into the Nav3 API or other libraries.

### Is Navigation 3 Compose Multiplatform (CMP) ready?
**Yes.** The Navigation 3 runtime depends on the Compose Runtime so is supported on all platforms that the Compose runtime supports. The Navigation 3 UI depends on Compose UI so is supported on all platforms where there is a Compose UI implementation. 

### Will Nav3 support Wear OS?
**Yes.** The team is working with Wear OS partners to bring Nav3 to the platform soon.

### Can I nest a `NavDisplay` inside another `NavDisplay`?
**Yes.** Since `NavDisplay` is just a composable, nesting works using standard Compose techniques.
*   **Use Case:** This is useful if you want different **transitions** for top-level stacks (e.g., bottom bar navigation) versus deeper stack navigation.
*   **Future Improvements:** For managing common UI elements like bottom navigation bars across screens, the team is developing **scene decorators** for version 1.1 to handle this without complex nesting.

---

## State Management & ViewModels

### How do I share a ViewModel between two screens?
You can achieve this in two main ways:
1.  **Custom Entry Decorator:** A custom decorator can control a `NavEntry`'s access to one or more `ViewModel`s. For example, a decorator could create a `ViewModel` that is shared between all entries on a back stack. Alternatively, an child entry could get access to a parent's view model by specifying the id of the parent in `metadata`. The custom decorator could then read that ID and provide the child entry with the parent's `ViewModel`.  
2.  **Hoisted State:** Use a shared state object accessible by the Nav entries.

*Note:* Instead of looking for a direct 1:1 replacement for Nav2's "NavGraph scoped ViewModels," consider hoisting state to a backstack-level decorator that provides context to all screens in that stack.

### Why do I have to manually add `rememberSavableStateHolderNavEntryDecorator`?
The team decided to give control back to the developers. While most apps should use it, allowing manual placement ensures you can control **where** the state is held or substitute it with your own implementation if necessary.

### Why is `SaveStateConfiguration` needed?
`SaveStateConfiguration` is used to store serializable values. A common use case is in multi-module apps where you need to inject a specific serializer for module-specific keys. In Nav3, the backstack primarily stores keys (identity), while actual state is stored in **decorators**.

---

## Transitions & Animations

### How do I implement Shared Element Transitions?
You implement them the same way you do in standard Compose. Because Nav3 does not change the underlying Compose system, you simply wrap your `NavDisplay` in a `SharedTransitionLayout` and apply the necessary modifiers to your content.

### Can I use shared element transitions between a screen and a Bottom Sheet?
**Generally, no.** This is a limitation of Compose, not Nav3. Shared elements require composables to be in the **same window**. Many bottom sheet implementations (like Dialogs) exist in a separate floating window, making shared transitions impossible.

### Why is defining animation metadata so verbose?
It was designed to be highly flexible to give developers control over every layer, but the team acknowledges the verbosity. They plan to introduce a **DSL (Domain Specific Language)** in version 1.1 to make defining metadata easier and more type-safe.

### Can I animate a bottom sheet popping when triggering it from code?
Not currently, as overlay scenes cannot currently signal animations back to the `NavDisplay`. However, this is being addressed in version 1.1, potentially via decorators that allow the system to wait for an exit animation before removing the content.

---

## Implementation Details

### When are Deep Links coming to Nav3?
Deep link support is already available via **recipes** and a comprehensive guide. Because developers own the backstack in Nav3, deep linking is simply a matter of parsing the intent and building the correct backstack manually.

### How can I combine different strategies (e.g., ListDetail, Dialog, BottomSheet)?
You can use the **`then` infix operator** to chain multiple strategies together. This creates a single strategy that evaluates the individual strategies in order.