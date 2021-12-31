plugins {
    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

group = "com.brys.poc.ig"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.javalin:javalin:4.1.1")
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("dev.kord:kord-core:0.8.0-M8")
}
val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "com.brys.poc.ig.RunKt"
    }
}
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    baseName = "apollo_image_generator"
    classifier = "Prod"
    version = "PRE_0.1.0"
}