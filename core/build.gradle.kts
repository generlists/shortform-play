import java.io.FileInputStream
import java.util.Properties

plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
    id ("com.google.dagger.hilt.android")
    id ("com.google.devtools.ksp")
}

var versionPropsFile = file("version.properties")
val versionProps: Properties = Properties()

if (versionPropsFile.canRead()) {
    versionProps.load(FileInputStream(versionPropsFile))
}

android {
    namespace = "com.sean.ratel.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "com.sean.ratel.core.HiltTestRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "VERSION_NAME", "\""+versionProps["VERSION_NAME"]+ "\"")
        buildConfigField("String", "LIB_NAME",  "\""+versionProps["LIB_NAME"]+ "\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        //실험에 동의했다.
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=androidx.media3.common.util.UnstableApi")
    }
    buildFeatures {
        buildConfig = true
    }
    hilt{
        enableAggregatingTask = false //https://ovso.tistory.com/475
    }
}

dependencies {

    implementation(project(":utils"))
    api(project(":android-youtube-player"))

    implementation(libs.core)
    implementation(libs.appcompat.appcompat)
    implementation(libs.material)
    implementation(libs.google.media3.common)
    implementation(libs.google.media3.exoplayer)
    implementation(libs.google.media3.ui)
    implementation(libs.google.media3.exoplayer.dash)
    implementation(libs.google.media3.exoplayer.hls)
    implementation(libs.google.media3.datasource.okhttp)

    implementation(libs.retrofit2)
    implementation (libs.retrofit2.converter.gson)


    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    debugImplementation(libs.okhttp.logging)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)


    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hilt.android.test)
    testImplementation(libs.hilt.google.test)

}