plugins {
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

dependencies {
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.compose.runtime)
}