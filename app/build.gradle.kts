import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.play.service)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.aboutlibraries)
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
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sean.ratel.android"
        minSdk = 28
        targetSdk = 36
        versionCode = 10082
        versionName = "1.0.8.2"
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

            if (localProperties.getProperty("ANDROID_KEYSTORE_PATH").isNullOrBlank()) {
                println("Release signing disabled (no keystore)")
            } else {
                storeFile = file(localProperties.getProperty("ANDROID_KEYSTORE_PATH") as String)
            }
            if (localProperties.getProperty("ANDROID_STORE_PASS").isNullOrBlank()) {
                println("Release signing disabled (no pass)")
            } else {
                storePassword = localProperties.getProperty("ANDROID_STORE_PASS") as String
            }
            if (localProperties.getProperty("ANDROID_KEY_ALIAS").isNullOrBlank()) {
                println("Release signing disabled (no alias)")
            } else {
                keyAlias = localProperties.getProperty("ANDROID_KEY_ALIAS") as String
            }
            if (localProperties.getProperty("ANDROID_KEY_PASS").isNullOrBlank()) {
                println("Release signing disabled (no key pass )")
            } else {
                keyPassword = localProperties.getProperty("ANDROID_KEY_PASS") as String
            }
        }
    }

    buildTypes {
        debug {

            buildConfigField(
                "String",
                "FIREBASE_BASE_URL",
                "\"${localProperties.getProperty("FIREBASE_BASE_URL")}\"",
            )
            buildConfigField(
                "String",
                "SHORTFORM_PLAY_BASE_URL",
                "\"${localProperties.getProperty("SHORTFORM_PLAY_BASE_URL")}\"",
            )
            buildConfigField(
                "String",
                "GOOGLE_CLOUD_PROJECT_NUMBER",
                "\"${localProperties.getProperty("GOOGLE_CLOUD_PROJECT_NUMBER")}\"",
            )
            // admob
            buildConfigField(
                "String",
                "BANNER_UNIT_ID",
                "\"${localProperties.getProperty("DEBUG_BANNER_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "NATIVE_AD_UNIT_ID",
                "\"${localProperties.getProperty("DEBUG_NATIVE_AD_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "ADAPTIVE_BANNER_UNIT_ID",
                "\"${localProperties.getProperty("DEBUG_ADAPTIVE_BANNER_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "INTERSTITIALAd_UNIT_ID",
                "\"${localProperties.getProperty("DEBUG_INTERSTITIALAd_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "Ad_OPEN_UNIT_ID",
                "\"${localProperties.getProperty("DEBUG_Ad_OPEN_UNIT_ID")}\"",
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
            buildConfigField(
                "String",
                "admobAppId",
                "\"${localProperties.getProperty("debug_admobAppId")}\"",
            )

            manifestPlaceholders["validator"] = "false"

            manifestPlaceholders["admobAppId"] =
                localProperties.getProperty(
                    "debug_admobAppId",
                    "ca-app-pub-3940256099942544~3347511713",
                )

            signingConfig = signingConfigs.getByName("debug")
        }
        release {

            buildConfigField(
                "String",
                "FIREBASE_BASE_URL",
                "\"${localProperties.getProperty("FIREBASE_BASE_URL")}\"",
            )
            buildConfigField(
                "String",
                "SHORTFORM_PLAY_BASE_URL",
                "\"${localProperties.getProperty("SHORTFORM_PLAY_BASE_URL")}\"",
            )
            buildConfigField(
                "String",
                "GOOGLE_CLOUD_PROJECT_NUMBER",
                "\"${localProperties.getProperty("GOOGLE_CLOUD_PROJECT_NUMBER")}\"",
            )

            // admob
            buildConfigField(
                "String",
                "BANNER_UNIT_ID",
                "\"${localProperties.getProperty("RELEASE_DEBUG_BANNER_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "NATIVE_AD_UNIT_ID",
                "\"${localProperties.getProperty("RELEASE_DEBUG_NATIVE_AD_UNIT_ID")}\"",
            )
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
            buildConfigField(
                "String",
                "Ad_OPEN_UNIT_ID",
                "\"${localProperties.getProperty("RELEASE_DEBUG_Ad_OPEN_UNIT_ID")}\"",
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
            buildConfigField(
                "String",
                "admobAppId",
                "\"${localProperties.getProperty("release_admobAppId")}\"",
            )

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            manifestPlaceholders["validator"] = "false"
            manifestPlaceholders["admobAppId"] =
                localProperties.getProperty("release_admobAppId", "")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
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

    implementation(libs.ai.shortformplay)
    implementation(libs.so.smartlab.sdk.common.ad.android)
    implementation(libs.so.smartlab.sdk.common.push.android)
    // androidx
    // Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat.appcompat)
    implementation(libs.androidx.material3)

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
    implementation(libs.androidx.compose.material3.material3)
    // Datastore
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.material)
    implementation(libs.com.android.installreferrer)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // hilt
    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.android.compiler)

    // google auth
    implementation(libs.google.accompanist.drawablepainter)

    // admob
    implementation(libs.google.play.service.ads)

    // oss
//    implementation(libs.google.play.service.oss)
    implementation(libs.aboutlibraries.compose.m3)

    // Network
    implementation(platform(libs.squareup.okhttp.bom))
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.okhttp.logging)
    implementation(libs.squareup.retrofit2)
    implementation(libs.squareup.retrofit2.converter.gson)
    implementation(libs.google.play.integrity)
    implementation(libs.com.auth.java.jwt)

    // image
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.video)
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

//    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
//
//    implementation("com.google.firebase:firebase-messaging")
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
