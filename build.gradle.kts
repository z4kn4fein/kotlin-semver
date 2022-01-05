import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeTargetPreset

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    kotlin("multiplatform") version "1.6.10"
}

group = "io.github.z4kn4fein"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

kotlin {
    explicitApi()

    jvm()
    js(BOTH) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }
    }

    presets.withType<AbstractKotlinNativeTargetPreset<*>>().forEach {
        targetFromPreset(it) {
            compilations.getByName("main") {
                defaultSourceSet.dependsOn(sourceSets["nativeMain"])
            }
        }
    }
}
