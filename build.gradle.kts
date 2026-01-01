plugins {
    base
    kotlin("jvm") version "2.3.0"
    kotlin("kapt") version "2.3.0"
}

group = "gg.grounds"
version = "1.0.0-SNAPSHOT"

allprojects {
    repositories {
        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")

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
}
