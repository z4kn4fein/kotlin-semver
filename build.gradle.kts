import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import java.net.URL
import kotlin.collections.mutableListOf

repositories {
    mavenCentral()
    google()
}

plugins {
    kotlin("multiplatform") version "1.7.21"
    kotlin("plugin.serialization") version "1.7.21"
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "1.7.20"
    id("org.sonarqube") version "3.5.0.2730"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
}

val kotlinx_serialization_version: String by project
val build_number: String get() = System.getenv("BUILD_NUMBER") ?: ""
val is_snapshot: Boolean get() = System.getProperty("snapshot") != null
val nativeMainSets: MutableList<KotlinSourceSet> = mutableListOf()
val nativeTestSets: MutableList<KotlinSourceSet> = mutableListOf()
val host: Host = getHostType()

version = "$version${if (is_snapshot) "-SNAPSHOT" else ""}"

kotlin {
    fun addNativeTarget(preset: KotlinTargetPreset<*>, desiredHost: Host) {
        val target = targetFromPreset(preset)
        nativeMainSets.add(target.compilations.getByName("main").kotlinSourceSets.first())
        nativeTestSets.add(target.compilations.getByName("test").kotlinSourceSets.first())
        if (host != desiredHost) {
            target.compilations.configureEach {
                compileKotlinTask.enabled = false
            }
        }
    }

    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }

    js(BOTH) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        nodejs()
    }

    // Windows
    addNativeTarget(presets["mingwX86"], Host.WINDOWS)
    addNativeTarget(presets["mingwX64"], Host.WINDOWS)

    // Linux
    addNativeTarget(presets["linuxArm64"], Host.LINUX)
    addNativeTarget(presets["linuxArm32Hfp"], Host.LINUX)
    addNativeTarget(presets["linuxX64"], Host.LINUX)

    // MacOS
    addNativeTarget(presets["macosX64"], Host.MAC_OS)
    addNativeTarget(presets["macosArm64"], Host.MAC_OS)

    // iOS
    addNativeTarget(presets["iosArm64"], Host.MAC_OS)
    addNativeTarget(presets["iosArm32"], Host.MAC_OS)
    addNativeTarget(presets["iosX64"], Host.MAC_OS)
    addNativeTarget(presets["iosSimulatorArm64"], Host.MAC_OS)

    // watchOS
    addNativeTarget(presets["watchosX86"], Host.MAC_OS)
    addNativeTarget(presets["watchosX64"], Host.MAC_OS)
    addNativeTarget(presets["watchosArm32"], Host.MAC_OS)
    addNativeTarget(presets["watchosArm64"], Host.MAC_OS)
    addNativeTarget(presets["watchosSimulatorArm64"], Host.MAC_OS)

    // tvOS
    addNativeTarget(presets["tvosArm64"], Host.MAC_OS)
    addNativeTarget(presets["tvosX64"], Host.MAC_OS)
    addNativeTarget(presets["tvosSimulatorArm64"], Host.MAC_OS)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinx_serialization_version")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val nativeTest by creating {
            dependsOn(commonTest)
        }

        configure(nativeMainSets) {
            dependsOn(nativeMain)
        }

        configure(nativeTestSets) {
            dependsOn(nativeTest)
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
        outputDirectory.get().walk()
            .filter { it.extension == "html" }
            .forEach { file ->
                val text = file.readText()
                file.writeText(
                    text.replace(
                        "<script src=\"https://unpkg.com/kotlin-playground@1\"></script>",
                        "<script src=\"https://unpkg.com/kotlin-playground@1\" data-selector=\"code\" data-server=\"https://pcsajtai-kotlin-compiler.onrender.com\"></script>"
                    ).replace(
                        "<button id=\"theme-toggle-button\"><span id=\"theme-toggle\"></span></button>",
                        "<a href=\"https://github.com/z4kn4fein/kotlin-semver\" target=\"_blank\" rel=\"noopener\" class=\"gh-link\"><i class=\"fa fa-github\"></i> <span class=\"repo-name\">z4kn4fein/kotlin-semver</span></a><button id=\"theme-toggle-button\"><span id=\"theme-toggle\"></span></button>"
                    ).replace(
                        "styles/custom.css\" rel=\"Stylesheet\">",
                        "styles/custom.css\" rel=\"Stylesheet\">" +
                            "<link href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\" rel=\"Stylesheet\">"
                    )
                )
            }
    }
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    dependsOn("dokkaHtml")
    from(buildDir.resolve("dokka"))
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
        property("sonar.projectVersion", "$version-$build_number")
        property("sonar.organization", "z4kn4fein")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sources", "src/commonMain/kotlin/io/github/z4kn4fein/semver")
        property("sonar.tests", "src/commonTest/kotlin/io/github/z4kn4fein/semver")
        property("sonar.kotlin.detekt.reportPaths", buildDir.resolve("reports/detekt/detekt.xml"))
        property("sonar.coverage.jacoco.xmlReportPaths", buildDir.resolve("reports/kover/xml/report.xml"))
    }
}

publishing {
    repositories {
        maven {
            name = "oss"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (is_snapshot) snapshotsRepoUrl else releasesRepoUrl
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
            description.set("Semantic Versioning library for Kotlin Multiplatform. It implements the full semantic version 2.0.0 specification and provides the ability to parse, compare, and increment semantic versions along with validation against constraints.")
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

    tasks.withType(AbstractPublishToMaven::class).configureEach {
        onlyIf { isPublicationAllowed(publication.name) }
    }

    tasks.withType(GenerateModuleMetadata::class).configureEach {
        onlyIf { isPublicationAllowed(publication.get().name) }
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

fun isPublicationAllowed(name: String): Boolean =
    when {
        name.startsWith("mingw") -> host == Host.WINDOWS
        name.startsWith("macos") ||
            name.startsWith("ios") ||
            name.startsWith("watchos") ||
            name.startsWith("tvos") -> host == Host.MAC_OS
        else -> host == Host.LINUX
    }

fun getHostType(): Host {
    val hostOs = System.getProperty("os.name")
    return when {
        hostOs.startsWith("Windows") -> Host.WINDOWS
        hostOs.startsWith("Mac") -> Host.MAC_OS
        hostOs == "Linux" -> Host.LINUX
        else -> throw Error("Invalid host.")
    }
}

enum class Host { WINDOWS, MAC_OS, LINUX }
