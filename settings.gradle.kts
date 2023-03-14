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
        maven(url = "https://jitpack.io")
        maven(url = "https://plugins.gradle.org/m2/")
    }
    versionCatalogs {
        create("libs") {
            from(files("./gradle/lib-versions.toml"))
        }
    }
}
//enableFeaturePreview("VERSION_CATALOGS")
rootProject.name = "WebRTC"
include(":app")
