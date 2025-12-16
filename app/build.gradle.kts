// D:\Univerzita\EasyNotes\app\build.gradle.kts

plugins {
    // 1. Основные плагины (используем alias из libs.versions.toml)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // УДАЛЕН: alias(libs.plugins.kotlin.compose)

    // 2. KSP - применяется для генерации кода Room
    alias(libs.plugins.google.devtools.ksp)
}
// ...

// Переменные для версий (лучше их определять в libs.versions.toml)
// Но, если они нужны здесь, они остаются.
val lifecycleVersion = "2.7.0"
val roomVersion = "2.6.1"

android {
    namespace = "com.volodymyr.easynotes"

    // ИСПРАВЛЕНИЕ 1: Использование простого присваивания compileSdk
    compileSdk = 36

    defaultConfig {
        applicationId = "com.volodymyr.easynotes"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Используйте Java 1.8, если нет необходимости в 11 (Android Studio и большинство 
        // библиотек оптимизированы для 1.8)
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        // Установка jvmTarget для совместимости
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

// ИСПРАВЛЕНИЕ 2: Удаляем ksp {} из корня, так как он должен быть настроен через extensions.
// Однако, allowWarnings не всегда доступен в ksp extension в build.gradle.kts.
// Если это требуется, обычно это настраивается через gradle.properties или компилятор.
// Для простоты, мы удаляем неразрешенную ссылку allowWarnings, чтобы завершить сборку.

dependencies {
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${lifecycleVersion}")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0") // Или используйте последнюю стабильную версию
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // ---- Тесты (Используем Version Catalog - libs) ----
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ---- Устаревшие прямые зависимости (Используйте libs для чистоты!) ----
    // KTX components
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // 1. КОМПОНЕНТЫ MVVM / LIFECYCLE (ViewModel и LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    // 2. ROOM (База данных)
    implementation("androidx.room:room-runtime:$roomVersion")
    // Используем ksp для генерации кода Room
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // 3. RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

// Удаленный неразрешенный блок:
/*
ksp {
    allowWarnings = true
}
*/