import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import java.net.URL
import kotlin.collections.mutableListOf

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

plugins {
    kotlin("multiplatform") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "1.6.21"
    id("org.sonarqube") version "3.3"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    id("org.jetbrains.kotlinx.kover") version "0.5.1"
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
}

val kotlinx_serialization_version: String by project
val build_number: String get() = System.getenv("BUILD_NUMBER") ?: ""
val is_snapshot: Boolean get() = System.getProperty("snapshot") != null

version = "$version${if (is_snapshot) "-SNAPSHOT" else ""}"

val nativeMainSets: MutableList<KotlinSourceSet> = mutableListOf()
val host: Host = getHostType()

kotlin {
    fun addNativeTarget(preset: KotlinTargetPreset<*>) {
        val target = targetFromPreset(preset)
        nativeMainSets.add(target.compilations.getByName("main").kotlinSourceSets.first())
    }

    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
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

    when (host) {
        Host.WINDOWS -> {
            addNativeTarget(presets["mingwX86"])
            addNativeTarget(presets["mingwX64"])
        }
        Host.LINUX -> {
            addNativeTarget(presets["linuxArm64"])
            addNativeTarget(presets["linuxArm32Hfp"])
            addNativeTarget(presets["linuxX64"])
        }
        Host.MAC_OS -> {
            addNativeTarget(presets["macosX64"])
            addNativeTarget(presets["macosArm64"])

            addNativeTarget(presets["iosArm64"])
            addNativeTarget(presets["iosArm32"])
            addNativeTarget(presets["iosX64"])
            addNativeTarget(presets["iosSimulatorArm64"])

            addNativeTarget(presets["watchosX86"])
            addNativeTarget(presets["watchosX64"])
            addNativeTarget(presets["watchosArm32"])
            addNativeTarget(presets["watchosArm64"])
            addNativeTarget(presets["watchosSimulatorArm64"])

            addNativeTarget(presets["tvosArm64"])
            addNativeTarget(presets["tvosX64"])
            addNativeTarget(presets["tvosSimulatorArm64"])
        }
    }

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

        configure(nativeMainSets) {
            dependsOn(nativeMain)
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
        property("sonar.coverage.jacoco.xmlReportPaths", buildDir.resolve("reports/kover/report.xml"))
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
}

signing {
    val signingKey = System.getenv("SIGNING_KEY") ?: ""
    val signingPassphrase = System.getenv("SIGNING_PASSPHRASE") ?: ""
    if (signingKey.isNotEmpty() && signingPassphrase.isNotEmpty()) {
        useInMemoryPgpKeys(signingKey, signingPassphrase)
        sign(publishing.publications)
    }
}

tasks.withType(AbstractPublishToMaven::class).configureEach {
    enabled = isTargetAllowedOnHost()
}

fun AbstractPublishToMaven.isTargetAllowedOnHost(): Boolean {
    return isTargetAllowedOnHost(publication.name)
}

fun isTargetAllowedOnHost(name: String): Boolean {
    return when (host) {
        Host.WINDOWS -> name.startsWith("mingw")
        Host.MAC_OS -> name.startsWith("macos") ||
            name.startsWith("ios") ||
            name.startsWith("watchos") ||
            name.startsWith("tvos")
        else -> true
    }
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
