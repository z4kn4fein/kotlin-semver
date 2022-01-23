import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeTargetPreset
import java.net.URL

plugins {
    kotlin("multiplatform") version "1.6.10"
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "1.6.10"
    id("org.sonarqube") version "3.3"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.jetbrains.kotlinx.kover") version "0.5.0-RC"
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

object Prop {
    val isSnapshot: Boolean get() = System.getProperty("snapshot") != null
    val buildNumber: String get() = System.getenv("BUILD_NUMBER") ?: ""
}

group = "io.github.z4kn4fein"
version = "$version${if (Prop.isSnapshot) "-SNAPSHOT" else ""}"

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

kotlin {
    explicitApi()

    jvm()
    js(BOTH) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        nodejs()
    }

    sourceSets {
        val commonMain by getting

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.kotest:kotest-assertions-core:5.+")
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }
    }

    presets.withType<AbstractKotlinNativeTargetPreset<*>>().forEach {
        if (it.isTargetAllowedOnHost()) {
            targetFromPreset(it) {
                compilations.getByName("main") {
                    defaultSourceSet.dependsOn(sourceSets["nativeMain"])
                }
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
        property("sonar.projectVersion", "$version-${Prop.buildNumber}")
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
            url = if (Prop.isSnapshot) snapshotsRepoUrl else releasesRepoUrl
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
            url.set("https://github.com/z4kn4fein/kotlin-semver")

            issueManagement {
                system.set("GitHub")
                url.set("https://github.com/z4kn4fein/kotlin-semver/issues")
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

fun getTargetHostType(name: String): HostType =
    when {
        name.startsWith("mingw") -> HostType.WINDOWS
        name.startsWith("macos") || name.startsWith("ios") || name.startsWith("watchos") ||
            name.startsWith("tvos") -> HostType.MAC_OS
        else -> HostType.LINUX
    }

fun AbstractPublishToMaven.isTargetAllowedOnHost(): Boolean {
    return isTargetAllowedOnHost(publication.name)
}

fun KotlinTargetPreset<*>.isTargetAllowedOnHost(): Boolean {
    return isTargetAllowedOnHost(name)
}

fun isTargetAllowedOnHost(name: String): Boolean {
    val os = OperatingSystem.current()
    return when (getTargetHostType(name)) {
        HostType.LINUX -> os.isLinux
        HostType.WINDOWS -> os.isWindows
        HostType.MAC_OS -> os.isMacOsX
    }
}

enum class HostType {
    MAC_OS, LINUX, WINDOWS
}
