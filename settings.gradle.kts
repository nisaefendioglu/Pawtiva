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
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Pawtiva"

include(":app")
include(":core:model")
include(":core:designsystem")
include(":core:domain")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":feature:onboarding")
include(":feature:home")
include(":feature:habits")
include(":feature:shop")
include(":feature:statistics")
include(":feature:settings")
