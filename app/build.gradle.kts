import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("com.google.android.gms.oss-licenses-plugin")
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ktlint)
}

ktlint {
    android.set(true)
    outputToConsole.set(true)
    enableExperimentalRules.set(true)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    debug.set(true)
}

val localProperties = Properties()
val localFile = rootProject.file("local.properties")

if (localFile.exists()) {
    localFile.inputStream().use { localProperties.load(it) }
}

android {
    namespace = "com.sean.ratel.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sean.ratel.android"
        minSdk = 28
        targetSdk = 35
        versionCode = 10060
        versionName = "1.0.6.0"
        testInstrumentationRunner = "com.sean.ratel.android.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {

        getByName("debug") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storePassword = "android"
        }
        create("release") {
            storeFile = file(localProperties.getProperty("ANDROID_KEYSTORE_PATH") as String)
            storePassword = localProperties.getProperty("ANDROID_STORE_PASS") as String
            keyAlias = localProperties.getProperty("ANDROID_KEY_ALIAS") as String
            keyPassword = localProperties.getProperty("ANDROID_KEY_PASS") as String
        }
    }

    buildTypes {
        debug {

            buildConfigField("String", "FIREBASE_BASE_URL", "\"${localProperties.getProperty("FIREBASE_BASE_URL")}\"")
            // admob
            buildConfigField("String", "BANNER_UNIT_ID", "\"${localProperties.getProperty("DEBUG_BANNER_UNIT_ID")}\"")
            buildConfigField("String", "NATIVE_AD_UNIT_ID", "\"${localProperties.getProperty("DEBUG_NATIVE_AD_UNIT_ID")}\"")
            buildConfigField("String", "ADAPTIVE_BANNER_UNIT_ID", "\"${localProperties.getProperty("DEBUG_ADAPTIVE_BANNER_UNIT_ID")}\"")
            buildConfigField("String", "INTERSTITIALAd_UNIT_ID", "\"${localProperties.getProperty("DEBUG_INTERSTITIALAd_UNIT_ID")}\"")
            buildConfigField("String", "Ad_OPEN_UNIT_ID", "\"${localProperties.getProperty("DEBUG_Ad_OPEN_UNIT_ID")}\"")

            buildConfigField("String", "MY_EMAIL_ACCOUNT", "\"${localProperties.getProperty("MY_EMAIL_ACCOUNT")}\"")
            buildConfigField("String", "NOTICES_URL", "\"${localProperties.getProperty("NOTICES_URL")}\"")
            buildConfigField("String", "NOTICES_URL_EN", "\"${localProperties.getProperty("NOTICES_URL_EN")}\"")
            buildConfigField("String", "REGAL_URL", "\"${localProperties.getProperty("REGAL_URL")}\"")
            buildConfigField("String", "REGAL_URL_EN", "\"${localProperties.getProperty("REGAL_URL_EN")}\"")

            manifestPlaceholders["validator"] = "false"
            manifestPlaceholders["admobAppId"] = localProperties.getProperty("debug_admobAppId")
            signingConfig = signingConfigs.getByName("debug")
        }
        release {

            buildConfigField("String", "FIREBASE_BASE_URL", "\"${localProperties.getProperty("FIREBASE_BASE_URL")}\"")
            // admob
            buildConfigField("String", "BANNER_UNIT_ID", "\"${localProperties.getProperty("RELEASE_DEBUG_BANNER_UNIT_ID")}\"")
            buildConfigField("String", "NATIVE_AD_UNIT_ID", "\"${localProperties.getProperty("RELEASE_DEBUG_NATIVE_AD_UNIT_ID")}\"")
            buildConfigField(
                "String",
                "ADAPTIVE_BANNER_UNIT_ID",
                "\"${localProperties.getProperty("RELEASE_DEBUG_ADAPTIVE_BANNER_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "INTERSTITIALAd_UNIT_ID",
                "\"${localProperties.getProperty("RELEASE_DEBUG_INTERSTITIALAd_UNIT_ID")}\"",
            )
            buildConfigField("String", "Ad_OPEN_UNIT_ID", "\"${localProperties.getProperty("RELEASE_DEBUG_Ad_OPEN_UNIT_ID")}\"")

            buildConfigField("String", "MY_EMAIL_ACCOUNT", "\"${localProperties.getProperty("MY_EMAIL_ACCOUNT")}\"")
            buildConfigField("String", "NOTICES_URL", "\"${localProperties.getProperty("NOTICES_URL")}\"")
            buildConfigField("String", "NOTICES_URL_EN", "\"${localProperties.getProperty("NOTICES_URL_EN")}\"")
            buildConfigField("String", "REGAL_URL", "\"${localProperties.getProperty("REGAL_URL")}\"")
            buildConfigField("String", "REGAL_URL_EN", "\"${localProperties.getProperty("REGAL_URL_EN")}\"")

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            manifestPlaceholders["validator"] = "false"
            manifestPlaceholders["admobAppId"] = localProperties.getProperty("release_admobAppId")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
        }
    }

    hilt {
        enableAggregatingTask = false // https://ovso.tistory.com/475
    }
}

dependencies {

//    implementation(project(":core"))
//    implementation(project(":ui"))
//    implementation(project(":utils"))

    implementation(libs.ai.shortformplay)
    // androidx
    // Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat.appcompat)

    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)

    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.material3.windowsize)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    // Datastore
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.material)

    // hilt
    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.android.compiler)

    // google auth
    implementation(libs.google.accompanist.drawablepainter)

    // admob
    implementation(libs.google.play.service.ads)

    // oss
    implementation(libs.google.play.service.oss)

    // Network
    implementation(platform(libs.squareup.okhttp.bom))
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.okhttp.logging)
    implementation(libs.squareup.retrofit2)
    implementation(libs.squareup.retrofit2.converter.gson)

    // image
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.lottie.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth.ktx)
    // Firebase Authentication
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    // Analytics
    implementation(libs.firebase.analytics)
    // firebase config
    implementation(libs.firebase.config)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.ext)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    androidTestImplementation(libs.google.hilt.android.test)
    testImplementation(libs.google.hilt.test)
}
