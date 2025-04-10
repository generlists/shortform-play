

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
val localProperties = java.util.Properties()
val localFile = File(rootDir, "local.properties")

if (localFile.exists()) {
    localFile.inputStream().buffered().use { stream ->
        localProperties.load(stream)
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
        maven {
            url = uri("https://maven.pkg.github.com/generlists/sdk-ratel-player-android")
            credentials {
                username = localProperties.getProperty("gpr.user")  ?: System.getenv("GPR_USER")
                password = localProperties.getProperty("gpr.key")  ?: System.getenv("GPR_TOKEN")
            }
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
include(":app")