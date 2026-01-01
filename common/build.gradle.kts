plugins { `java-library` }

dependencies {
    api("redis.clients:jedis:3.9.0")

    testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
