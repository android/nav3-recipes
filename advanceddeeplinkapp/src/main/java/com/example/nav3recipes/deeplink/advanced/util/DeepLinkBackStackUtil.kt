package com.example.nav3recipes.deeplink.advanced.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.nav3recipes.common.deeplink.LIST_USERS
import com.example.nav3recipes.deeplink.advanced.DEEPLINK_URL_TAG_USER
import com.example.nav3recipes.deeplink.advanced.DEEPLINK_URL_TAG_USERS
import com.example.nav3recipes.deeplink.advanced.Home
import com.example.nav3recipes.deeplink.advanced.NavDeepLinkRecipeKey
import com.example.nav3recipes.deeplink.advanced.UserDetail
import com.example.nav3recipes.deeplink.advanced.Users

/**
 * A function that build a synthetic backStack.
 *
 * This helper returns one of two possible backStacks:
 *
 * 1. a backStack with only the deeplinked key if [buildFullPath] is false.
 * 2. a backStack containing the deeplinked key and its hierarchical parent keys
 * if [buildFullPath] is true.
 *
 * In the context of this recipe, [buildFullPath] is true if the deeplink intent has the
 * [android.content.Intent.FLAG_ACTIVITY_NEW_TASK] and [android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK]
 * flags.
 * These flags indicate that the deeplinked Activity was started as the root Activity of a new Task, in which case
 * a full synthetic backStack is required in order to support the proper, expected back button behavior.
 *
 * If those flags were not present, it means the deeplinked Activity was started
 * in the app that originally triggered the deeplink. In this case, that original app is assumed to
 * already have existing screens that users can system back into, therefore a synthetic backstack
 * is OPTIONAL.
 *
 */
internal fun buildBackStack(
    startKey: NavKey,
    buildFullPath: Boolean
): List<NavKey> {
    if (!buildFullPath) return listOf(startKey)
    /**
     * iterate up the parents of the startKey until it reaches the root key (a key without a parent)
     */
    return buildList {
        var node: NavKey? = startKey
        while (node != null) {
            add(0, node)
            val parent = if (node is NavDeepLinkRecipeKey) {
                node.parent
            } else null
            node = parent
        }
    }
}

/**
 * If this app was started on its own Task stack, then navigate up would simply
 * pop from the backStack.
 *
 * Otherwise, it will restart this app in a new Task and build a full synthetic backStack
 * starting from the root key to current key's parent (current key is "popped" upon user click on up button).
 * This operation is required because by definition, an Up button is expected to:
 * 1. Move from current screen to its hierarchical parent
 * 2. Stay within this app
 *
 * Therefore, we need to build a synthetic backStack to fulfill expectation 1., and we need to
 * restart the app in its own Task so that this app's screens are displayed within
 * this app instead of being displayed within the originating app that triggered the deeplink.
 */
internal fun NavBackStack<NavKey>.navigateUp(
    activity: Activity,
    context: Context
) {
    /**
     * The root key (the first key on synthetic backStack) would/should never display the Up button.
     * So if the backStack only contains a non-root key, it means a synthetic backStack had not
     * been built (aka the app was opened in the originating Task).
     */
    if (size == 1) {
        val currKey = last()
        /**
         * upon navigating up, the current key is popped, so the restarted activity
         * lands on the current key's parent
         */
        val deeplinkKey = if (currKey is NavDeepLinkRecipeKey) {
            currKey.parent
        } else null

        /**
         * create a [androidx.core.app.TaskStackBuilder] that will restart the
         * Activity as the root Activity of a new Task
         */
        val builder = createTaskStackBuilder(deeplinkKey, activity, context)
        // ensure current activity is finished
        activity.finish()
        // trigger restart
        builder.startActivities()
    } else {
        removeLastOrNull()
    }

}

/**
 *  Creates a [androidx.core.app.TaskStackBuilder].
 *
 *  The builder takes the current context and Activity and builds a new Task stack with the
 *  restarted activity as the root Activity. The resulting TaskStack is used to restart
 *  the Activity in its own Task.
 */
private fun createTaskStackBuilder(
    deeplinkKey: NavKey?,
    activity: Activity,
    context: Context
): TaskStackBuilder {
    /**
     * The intent to restart the current activity.
     */
    val intent = Intent(context, activity.javaClass)

    /**
     * Pass in the deeplink url of the target key so that upon restart, the app
     * can build the synthetic backStack starting from the deeplink key all the way up to the
     * root key.
     *
     * See [buildBackStack] for building synthetic backStack.
     */
    if (deeplinkKey != null && deeplinkKey is NavDeepLinkRecipeKey) {
        intent.data = deeplinkKey.deeplinkUrl.toUri()
    }

    /**
     * Ensure that the Activity is restarted as the root of a new Task
     */
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

    /**
     * Lastly, attach the intent to the TaskStackBuilder.
     *
     * By using `addNextIntentWithParentStack`, the TaskStackBuilder will automatically
     * add the intents for the parent activities (if any) of [activity].
     */
    return TaskStackBuilder.create(context).addNextIntentWithParentStack(intent)
}

/**
 * A function that converts a deeplink uri into a NavKey.
 *
 * This helper is intentionally simple and basic. For a recipe that focuses on parsing a
 * deeplink uri into a NavKey, please see [com.example.nav3recipes.deeplink.basic].
 */
internal fun Uri?.toKey(): NavKey {
    if (this == null) return Home

    val paths = pathSegments

    if (pathSegments.isEmpty()) return Home

    return when(paths.first()) {
        DEEPLINK_URL_TAG_USERS -> Users
        DEEPLINK_URL_TAG_USER -> {
            val firstName = pathSegments[1]
            val location = pathSegments[2]
            val user = LIST_USERS.find {
                it.firstName == firstName && it.location == location
            }
            if (user == null) Users else UserDetail(user)
        }
        else -> Home
    }
}