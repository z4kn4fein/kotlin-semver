import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

repositories {
    mavenCentral()
    google()
}

plugins {
    kotlin("multiplatform") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.sonarqube") version "5.0.0.4638"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

val kotlinxSerializationVersion: String by project
val buildNumber: String get() = System.getenv("BUILD_NUMBER") ?: ""
val isSnapshot: Boolean get() = System.getProperty("snapshot") != null

version = "$version${if (isSnapshot) "-SNAPSHOT" else ""}"

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
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
    linuxArm32Hfp()

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            }
        }
    }
}

tasks.getByName<DokkaTask>("dokkaHtml") {
    outputDirectory.set(file(buildDir.resolve("dokka")))
    dokkaSourceSets {
        configureEach {
            includes.from("Module.md")
            samples.from("src/commonTest/kotlin/io/github/z4kn4fein/semver/samples")
            sourceLink {
                localDirectory.set(file("src/commonMain/kotlin"))
                remoteUrl.set(URL("https://github.com/z4kn4fein/kotlin-semver/blob/master/src/commonMain/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }

    val json = """{ "customStyleSheets": ["${file(project.projectDir.resolve("assets/custom.css"))}"] }"""
    pluginsMapConfiguration.set(mapOf("org.jetbrains.dokka.base.DokkaBase" to json.replace("\\", "/")))

    doLast {
        outputDirectory.getAsFileTree().getFiles()
            .filter { it.extension == "html" }
            .forEach { file ->
                var text = file.readText()
                if (!(file.parent?.endsWith("dokka") ?: false)) {
                    text = text.replace(
                        "<script type=\"text/javascript\" src=\"https://unpkg.com/kotlin-playground@1/dist/playground.min.js\" async=\"async\"></script>",
                        "<script type=\"text/javascript\" src=\"https://unpkg.com/kotlin-playground@1\" data-selector=\"code\" " +
                                "data-server=\"https://pcsajtai-kotlin-compiler.onrender.com\"></script>"
                    )
                }
                file.writeText(
                    text.replace(
                        "<button class=\"navigation-controls--btn navigation-controls--theme\" id=\"theme-toggle-button\" type=\"button\">switch theme</button>",
                        "<a href=\"https://github.com/z4kn4fein/kotlin-semver\" target=\"_blank\" rel=\"noopener\" class=\"gh-link\"><i class=\"fa fa-github\"></i> <span class=\"repo-name\">" +
                        "z4kn4fein/kotlin-semver</span></a><button class=\"navigation-controls--btn navigation-controls--theme\" id=\"theme-toggle-button\" type=\"button\">switch theme</button>"
                    ).replace(
                        "styles/custom.css\" rel=\"Stylesheet\">",
                        "styles/custom.css\" rel=\"Stylesheet\">" +
                        "<link href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\" rel=\"Stylesheet\">"
                    )
                )
            }
    }
}

val javadocJar =
    tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        dependsOn("dokkaHtml")
        from(buildDir.resolve("dokka"))
    }

detekt {
    buildUponDefaultConfig = true
    parallel = true
    isIgnoreFailures = true
}

ktlint {
    filter {
        exclude { element -> element.file.path.contains("build.gradle.kts") }
    }
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

rootProject.the<NodeJsRootExtension>().apply {
    nodeVersion = "22.0.0-nightly202404032241e8c5b3"
    nodeDownloadBaseUrl = "https://nodejs.org/download/nightly"
}

tasks.withType<KotlinNpmInstallTask>().configureEach {
    args.add("--ignore-engines")
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
        property("sonar.kotlin.detekt.reportPaths", buildDir.resolve("reports/detekt/detekt.xml"))
        property("sonar.coverage.jacoco.xmlReportPaths", buildDir.resolve("reports/kover/report.xml"))
    }
}

publishing {
    repositories {
        maven {
            name = "oss"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (isSnapshot) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }

    publications.withType<MavenPublication> {
        artifact(javadocJar.get())

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
}

signing {
    val signingKey = System.getenv("SIGNING_KEY") ?: ""
    val signingPassphrase = System.getenv("SIGNING_PASSPHRASE") ?: ""
    if (signingKey.isNotEmpty() && signingPassphrase.isNotEmpty()) {
        useInMemoryPgpKeys(signingKey, signingPassphrase)
        sign(publishing.publications)
    }
}
