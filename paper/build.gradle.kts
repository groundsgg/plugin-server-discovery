plugins {
    id("com.gradleup.shadow") version "9.3.0"
}

dependencies {
    implementation(project(":common"))
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("") // Removes the 'all' classifier
    archiveVersion.set("") // Removes the version from the jar name
}
