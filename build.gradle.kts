plugins {
    base
    kotlin("jvm") version "2.3.0"
    kotlin("kapt") version "2.3.0"
    id("com.diffplug.spotless") version "8.1.0"
}

group = "gg.grounds"

version = "1.0.0-SNAPSHOT"

allprojects {
    apply(plugin = "com.diffplug.spotless")

    repositories {
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        mavenCentral()
    }

    spotless {
        kotlin {
            ktfmt().googleStyle().configure {
                it.setBlockIndent(4)
                it.setContinuationIndent(4)
            }
        }
        kotlinGradle {
            ktfmt().googleStyle().configure {
                it.setBlockIndent(4)
                it.setContinuationIndent(4)
            }
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")

    kotlin { jvmToolchain(25) }

    tasks.test {
        useJUnitPlatform()

        testLogging {
            // Show assertion diffs in test output
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}
