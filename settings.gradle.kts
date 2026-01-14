/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // Uncomment and change the build ID if you need to use snapshot artifacts.
        // See androidx.dev for full instructions.
        /*maven {
            url = uri("https://androidx.dev/snapshots/builds/<build_id>/artifacts/repository")
        }*/
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Uncomment and change the build ID if you need to use snapshot artifacts.
        // See androidx.dev for full instructions.
        /*maven {
            url = uri("https://androidx.dev/snapshots/builds/<build_id>/artifacts/repository")
        }*/
    }
}

rootProject.name = "Nav3Recipes"
include(":app")
include(":advanceddeeplinkapp")
include(":common")
include(":lint-rules")
