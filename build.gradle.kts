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
//        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.18.1")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}

plugins {
    id("io.gitlab.arturbosch.detekt").version("1.18.1")
}

detekt {
    toolVersion = "1.18.1"
    config = files("$rootDir/config/detekt.yml")
    buildUponDefaultConfig = true
    source.setFrom(listOf("$rootDir/app/src/main/java", "$rootDir/app/src/main/kotlin"))
    config.setFrom(listOf("$rootDir/config/detekt/statistics.yml", "$rootDir/config/detekt/detekt.yml"))
    allRules = true
    parallel = true
    baseline = file("$rootDir/config/detekt/baseline.xml")
    group = "verification"

    reports {
        xml {
            enabled = true
            destination = file("$rootDir/app/build/detekt/detekt.xml")
        }
        html {
            enabled = true
            destination = file("$rootDir/app/build/detekt/detekt.html")
        }
        txt {
            enabled = true
            destination = file("$rootDir/app/build/detekt/detekt.txt")
        }
    }
}

val analysisDir = file(projectDir)
val configDir = "$rootDir/config/detekt"
val reportDir = "$rootDir/app/build/detekt"
val configFile = file("$configDir/detekt.yml")
val baselineFile = file("$configDir/baseline.xml")
val statisticsConfigFile = file("$configDir/statistics.yml")

val kotlinFiles = "**/*.kt"
val kotlinScriptFiles = "**/*.kts"
val resourceFiles = "**/resources/**"
val buildFiles = "**/build/**"

val detektFormat by tasks.registering(io.gitlab.arturbosch.detekt.Detekt::class) {
    description = "Formats whole project."
    parallel = true
    disableDefaultRuleSets = true
    buildUponDefaultConfig = true
    autoCorrect = true
    setSource(analysisDir)
    config.setFrom(listOf(statisticsConfigFile, configFile))
    include(kotlinFiles)
    include(kotlinScriptFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
    baseline.set(baselineFile)
    reports {
        xml.enabled = false
        html.enabled = false
        txt.enabled = false
    }
}

val detektProjectBaseline by tasks.registering(io.gitlab.arturbosch.detekt.DetektCreateBaselineTask::class) {
    description = "Overrides current baseline."
    buildUponDefaultConfig.set(true)
    ignoreFailures.set(true)
    parallel.set(true)
    setSource(analysisDir)
    config.setFrom(listOf(statisticsConfigFile, configFile))
    include(kotlinFiles)
    include(kotlinScriptFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
    baseline.set(baselineFile)
}

tasks.register(name = "detektAll", type = io.gitlab.arturbosch.detekt.Detekt::class) {
    description = "Runs the whole project at once."
    parallel = true
    buildUponDefaultConfig = true
    setSource(analysisDir)
    config.setFrom(listOf(statisticsConfigFile, configFile))
    include(kotlinFiles)
    include(kotlinScriptFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
    baseline.set(baselineFile)
    group = "verification"
    reports {
        xml {
            enabled = true
            destination = file("$reportDir/detekt.xml")
        }
        html {
            enabled = true
            destination = file("$reportDir/detekt.html")
        }
        txt {
            enabled = true
            destination = file("$reportDir/detekt.txt")
        }
    }
}

