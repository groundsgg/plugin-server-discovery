plugins {
    base
    kotlin("jvm") version "2.3.0"
    kotlin("kapt") version "2.3.0"
    id("maven-publish")
}

allprojects {
    repositories {
        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        mavenCentral()
    }
}

subprojects {
    group = "gg.grounds"

    val versionOverride = project.findProperty("versionOverride") as? String
    version = versionOverride ?: "local-SNAPSHOT"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "maven-publish")

    kotlin {
        jvmToolchain(25)
    }

    tasks.test {
        useJUnitPlatform()

        testLogging {
            // Show assertion diffs in test output
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/groundsgg")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }

        publications {
            create<MavenPublication>("java") {
                from(components["java"])
                artifactId = rootProject.name + "-" + project.name
            }
        }
    }
}
