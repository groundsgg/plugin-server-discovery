plugins {
    id("com.gradleup.shadow") version "9.3.0"
    id("com.github.gmazzo.buildconfig") version "6.0.7"
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

tasks.build { dependsOn(tasks.shadowJar) }

tasks.jar { enabled = false }

tasks.shadowJar {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("") // Removes the 'all' classifier
    archiveVersion.set("") // Removes the version from the jar name
}

buildConfig {
    className("BuildInfo")
    packageName("gg.grounds")

    useKotlinOutput()

    buildConfigField("String", "VERSION", "\"${project.version}\"")
}
