plugins {
    id("com.gradleup.shadow") version "9.3.0"
    id("com.opencastsoftware.gradle.buildinfo") version "0.3.1"
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

tasks.build { dependsOn(tasks.shadowJar) }

tasks
    .matching { it.name == "kaptGenerateStubsKotlin" }
    .configureEach { dependsOn(tasks.generateBuildInfo) }

tasks.jar { enabled = false }

tasks.shadowJar {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("") // Removes the 'all' classifier
    archiveVersion.set("") // Removes the version from the jar name
}

buildInfo {
    packageName.set("gg.grounds")
    className.set("BuildInfo")
    properties.set(mapOf("version" to "${project.version}"))
}
