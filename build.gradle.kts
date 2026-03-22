// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.devtools.ksp)  apply  false
    alias(libs.plugins.google.play.service) apply false
    alias(libs.plugins.aboutlibraries) apply false
    // id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    // id("com.android.library") version "8.2.2" apply false
    //id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
    alias(libs.plugins.hilt.android) apply false
   // id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.ktlint)
}

//alias(libs.plugins.android.application) apply false
//alias(libs.plugins.kotlin.android) apply false
//alias(libs.plugins.kotlin.compose) apply false
//alias(libs.plugins.android.library) apply false
//alias(libs.plugins.hilt.android) apply false
//alias(libs.plugins.devtools.ksp)  apply  false
//alias(libs.plugins.aboutlibraries) apply false
//alias(libs.plugins.google.play.service) apply false
//alias(libs.plugins.google.firebase.crashlytics) apply false
