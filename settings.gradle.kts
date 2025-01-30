include(":app")

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
        maven {
            url = uri("https://jitpack.io")
        }
    }
}
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // oss-licenses-plugin 클래스패스 추가 //8.2.2 gradle 때문에 일부 class path 로 이동(build.gradle.kts)
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.5")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
}

rootProject.name = "shortform-play"
include(":android-youtube-player")
include(":app")
include(":core")
include(":utils")
include(":ui")
