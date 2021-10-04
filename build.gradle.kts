// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")

        classpath("com.google.dagger:hilt-android-gradle-plugin:2.39.1")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.18.1")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("io.gitlab.arturbosch.detekt").version("1.18.1")
}
detekt {
    toolVersion = "1.18.1"
    config = files("$rootDir/config/detekt.yml")
    buildUponDefaultConfig = true

    reports {
        xml {
            enabled = true
            destination = file("$rootDir/config/destination.xml")
        }
        html {
            enabled = true
            destination = file("$rootDir/config/destination.html")
        }
        txt {
            enabled = true
            destination = file("$rootDir/config/destination.txt")
        }
    }
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}
