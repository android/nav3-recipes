package com.example.nav3recipes.deeplink.usecases.matcher.modules

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.RequestExtrasKey
import com.example.nav3recipes.deeplink.usecases.matcher.JsonDeepLinkMatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoSet
import kotlinx.serialization.Serializable

@Serializable
data class ProfileKey(val userId: String) : NavKey

data object ProfileDeepLinkKey : RequestExtrasKey<String>

@Module
@InstallIn(ActivityComponent::class)
object ProfileMatcherModule {

    @Provides
    @IntoSet
    fun provideProfileMatcher(): DeepLinkMatcher<NavKey, DeepLinkMatcher.MatchResult<NavKey>> {
        return JsonDeepLinkMatcher(
            ProfileDeepLinkKey,
            ProfileKey.serializer()
        )
    }
}
