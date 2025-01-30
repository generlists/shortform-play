plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
    id ("com.google.dagger.hilt.android")
    id ("com.google.devtools.ksp")
}

android {
    namespace = "com.sean.ratel.ui"
    compileSdk = 34

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
        //실험에 동의했다.
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=androidx.media3.common.util.UnstableApi")
    }
    hilt{
        enableAggregatingTask = false //https://ovso.tistory.com/475
    }
}
val mediaVersion = "1.2.1"
val hiltVersion = "2.49"

dependencies {
    implementation(project(":core"))
    implementation(project(":utils"))
    implementation(libs.core)
    implementation(libs.appcompat.appcompat)

    //compose
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation( libs.compose.material)


    implementation(libs.hilt.android)
    implementation(libs.androidx.constraintlayout)
    debugImplementation(libs.ui.tooling)
    ksp(libs.hilt.android.compiler)


    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hilt.android.test)
    testImplementation(libs.hilt.google.test)

}

