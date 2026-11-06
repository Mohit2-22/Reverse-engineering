pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Mapbox SDK repo — re-enable in Sprint 3 (needs MAPBOX_DOWNLOADS_TOKEN):
        // maven("https://api.mapbox.com/downloads/v2/releases/maven") {
        //     authentication { create<BasicAuthentication>("basic") }
        //     credentials {
        //         username = "mapbox"
        //         password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").orNull ?: ""
        //     }
        // }
    }
}

rootProject.name = "FitTrack"
include(":app")
