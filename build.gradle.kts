@file:OptIn(ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import io.gitlab.arturbosch.detekt.Detekt
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

repositories {
    mavenCentral()
    google()
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.mavenPublish)
}

val buildNumber: String get() = System.getenv("BUILD_NUMBER") ?: ""
val isSnapshot: Boolean get() = System.getProperty("snapshot") != null

version = "$version${if (isSnapshot) "-SNAPSHOT" else ""}"

kotlin {
    explicitApi()

    jvm {
        compilerOptions.jvmTarget = JvmTarget.JVM_1_8
    }

    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        nodejs()
    }

    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        nodejs()
    }

    wasmWasi {
        nodejs()
    }

    macosX64()
    macosArm64()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    mingwX64()

    linuxX64()
    linuxArm64()

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.serialization.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.serialization.json)
        }
    }
}

dokka {
    moduleName.set("semver")
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka"))
    }
    dokkaSourceSets.commonMain {
        includes.from("Module.md")
        samples.from("src/commonTest/kotlin/io/github/z4kn4fein/semver/samples")
        sourceLink {
            localDirectory.set(file("src/commonMain/kotlin"))
            remoteUrl("https://github.com/z4kn4fein/kotlin-semver/blob/master/src/commonMain/kotlin")
            remoteLineSuffix.set("#L")
        }
    }

    pluginsConfiguration.html {
        customStyleSheets.from("docs/styles.css")
        templatesDir.set(file("docs/templates"))
    }
}

val buildDocs by tasks.registering {
    dependsOn(tasks.dokkaGenerate)
    doLast {
        fileTree(layout.buildDirectory.dir("dokka"))
            .filter { it.extension == "html" }
            .forEach { file ->
                var text = file.readText()
                if (!(file.parent?.endsWith("dokka") ?: false)) {
                    file.writeText(text.replace(
                        "<script type=\"text/javascript\" src=\"https://unpkg.com/kotlin-playground@1/dist/playground.min.js\" async=\"async\"></script>",
                        "<script type=\"text/javascript\" src=\"https://unpkg.com/kotlin-playground@1\" data-selector=\"code\" " +
                                "data-server=\"https://kotlin-compiler-4own5.ondigitalocean.app\"></script>"
                    ))
                }
            }
    }
}

ktlint {
    filter {
        exclude { element -> element.file.path.contains("build.gradle.kts") }
    }
}

detekt {
    buildUponDefaultConfig = true
    parallel = true
    isIgnoreFailures = true
}

tasks.withType<Detekt>().configureEach {
    setSource(project.files(project.projectDir.resolve("src/commonMain")))
    include("**/*.kt")
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "z4kn4fein_kotlin-semver")
        property("sonar.projectName", "kotlin-semver")
        property("sonar.projectVersion", "$version-$buildNumber")
        property("sonar.organization", "z4kn4fein")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sources", "src/commonMain/kotlin/io/github/z4kn4fein/semver")
        property("sonar.tests", "src/commonTest/kotlin/io/github/z4kn4fein/semver")
        property("sonar.kotlin.detekt.reportPaths", layout.buildDirectory.file("reports/detekt/detekt.xml"))
        property("sonar.coverage.jacoco.xmlReportPaths", layout.buildDirectory.file("reports/kover/report.xml"))
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    if (providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey").isPresent &&
        providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword").isPresent) {
        signAllPublications()
    }

    configure(KotlinMultiplatform(
        javadocJar = JavadocJar.Dokka(buildDocs.name),
        sourcesJar = true
    ))

    coordinates(group as String?, "semver", version as String?)

    pom {
        name.set("Kotlin Semantic Versioning")
        description.set(
            "Semantic Versioning library for Kotlin Multiplatform. It implements the full semantic version " +
                    "2.0.0 specification and provides the ability to parse, compare, and increment semantic " +
                    "versions along with validation against constraints.",
        )
        url.set("https://z4kn4fein.github.io/kotlin-semver")

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/z4kn4fein/kotlin-semver/issues")
        }
        ciManagement {
            system.set("GitHub Actions")
            url.set("https://github.com/z4kn4fein/kotlin-semver/actions")
        }
        licenses {
            license {
                name.set("MIT License")
                url.set("https://raw.githubusercontent.com/z4kn4fein/kotlin-semver/main/LICENSE")
            }
        }
        developers {
            developer {
                id.set("z4kn4fein")
                name.set("Peter Csajtai")
                email.set("peter.csajtai@gmail.com")
            }
        }
        scm {
            url.set("https://github.com/z4kn4fein/kotlin-semver")
        }
    }
}
