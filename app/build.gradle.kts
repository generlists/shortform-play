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
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sean.ratel.android"
        minSdk = 28
        targetSdk = 34
        versionCode = 10020
        versionName = "1.0.2"
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

            buildConfigField(
                "String",
                "FIREBASE_BASE_URL",
                "\"${localProperties.getProperty("FIREBASE_BASE_URL")}\"",
            )
            // admob
            buildConfigField(
                "String",
                "BANNER_UNIT_ID",
                "\"${localProperties.getProperty("BANNER_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "NATIVE_AD_UNIT_ID",
                "\"${localProperties.getProperty("NATIVE_AD_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "ADAPTIVE_BANNER_UNIT_ID",
                "\"${localProperties.getProperty("ADAPTIVE_BANNER_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "INTERSTITIALAd_UNIT_ID",
                "\"${localProperties.getProperty("INTERSTITIALAd_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "Ad_OPEN_UNIT_ID",
                "\"${localProperties.getProperty("Ad_OPEN_UNIT_ID")}\"",
            )

            buildConfigField(
                "String",
                "MY_EMAIL_ACCOUNT",
                "\"${localProperties.getProperty("MY_EMAIL_ACCOUNT")}\"",
            )
            buildConfigField(
                "String",
                "NOTICES_URL",
                "\"${localProperties.getProperty("NOTICES_URL")}\"",
            )
            buildConfigField(
                "String",
                "NOTICES_URL_EN",
                "\"${localProperties.getProperty("NOTICES_URL_EN")}\"",
            )
            buildConfigField(
                "String",
                "REGAL_URL",
                "\"${localProperties.getProperty("REGAL_URL")}\"",
            )
            buildConfigField(
                "String",
                "REGAL_URL_EN",
                "\"${localProperties.getProperty("REGAL_URL_EN")}\"",
            )

            manifestPlaceholders["validator"] = "false"
            manifestPlaceholders["admobAppId"] = localProperties.getProperty("admobAppId")
            signingConfig = signingConfigs.getByName("debug")
        }
        release {

            buildConfigField(
                "String",
                "FIREBASE_BASE_URL",
                "\"${localProperties.getProperty("FIREBASE_BASE_URL")}\"",
            )
            // admob
            buildConfigField(
                "String",
                "BANNER_UNIT_ID",
                "\"${localProperties.getProperty("BANNER_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "NATIVE_AD_UNIT_ID",
                "\"${localProperties.getProperty("NATIVE_AD_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "ADAPTIVE_BANNER_UNIT_ID",
                "\"${localProperties.getProperty("ADAPTIVE_BANNER_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "INTERSTITIALAd_UNIT_ID",
                "\"${localProperties.getProperty("INTERSTITIALAd_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "Ad_OPEN_UNIT_ID",
                "\"${localProperties.getProperty("Ad_OPEN_UNIT_ID")}\"",
            )

            buildConfigField(
                "String",
                "MY_EMAIL_ACCOUNT",
                "\"${localProperties.getProperty("MY_EMAIL_ACCOUNT")}\"",
            )
            buildConfigField(
                "String",
                "NOTICES_URL",
                "\"${localProperties.getProperty("NOTICES_URL")}\"",
            )
            buildConfigField(
                "String",
                "NOTICES_URL_EN",
                "\"${localProperties.getProperty("NOTICES_URL_EN")}\"",
            )
            buildConfigField(
                "String",
                "REGAL_URL",
                "\"${localProperties.getProperty("REGAL_URL")}\"",
            )
            buildConfigField(
                "String",
                "REGAL_URL_EN",
                "\"${localProperties.getProperty("REGAL_URL_EN")}\"",
            )

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            manifestPlaceholders["validator"] = "false"
            manifestPlaceholders["admobAppId"] = localProperties.getProperty("admobAppId")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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

    implementation("ai.shortform-play.sdk.ratel.player.android:player-core:0.0.0.1")

    // Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.appcompat.appcompat)
    implementation(libs.androidx.fragment.ktx)

    // compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lottie.compose)

    implementation(libs.androidx.compose.material)
    implementation(libs.google.accompanist.drawablepainter)
    implementation(libs.google.accompanist.systemuicontroller)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.constraintlayout)

    // implementation(libs.material)
    implementation(libs.androidx.navigation.compose)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.gson)

    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.lottie.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)

    // Firebase Authentication
//    implementation(libs.firebase.auth)
    // implementation(libs.compose.material3.windowsize)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.hilt.android.test)
    testImplementation(libs.hilt.google.test)

    // oss
    implementation(libs.google.play.service.oss)
    // admob
    implementation(libs.google.play.service.ads)

//    implementation(libs.google.api.client.gson)
//
//    // google auth
// //    implementation(libs.google.play.service.auth)
// //    implementation(libs.google.api.service.youtube)
// //    implementation(libs.google.oauth.client.jetty)
// //    implementation(libs.google.http.client.android)
// //    implementation(libs.google.api.client.android)
//
}
