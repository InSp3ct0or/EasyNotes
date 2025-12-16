// D:\Univerzita\EasyNotes\settings.gradle.kts

pluginManagement {
    repositories {
        // Убедитесь, что Google и Maven Central стоят первыми!
        google()
        mavenCentral()
        gradlePluginPortal()
        // Если этот репозиторий дублирует Google(), его можно удалить:
        // maven { url = uri("https://dl.google.com/dl/android/maven2/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EasyNotes"
include(":app")