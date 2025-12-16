plugins {
    // Android AGP
    id("com.android.application") version "8.3.2" apply false

    // Основной плагин Kotlin
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false

    // УДАЛЕН: id("org.jetbrains.kotlin.plugin.compose") version "1.9.22" apply false

    // Плагин генерации кода
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}