plugins {
    kotlin("jvm") version "1.5.10"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id ("com.google.cloud.tools.jib") version "3.1.4"
}

group = "com.brys.poc.ig"
version = "1.0-snapshot"

application {
    mainClassName = "com.brys.poc.ig.RunKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.javalin:javalin:4.3.0")
    implementation("ch.qos.logback:logback-classic:1.2.10")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
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

jib {
    from {
        image = "openjdk:18-jdk-buster"
    }
    container {
        mainClass = "com.brys.poc.ig.RunKt"

    }
    extraDirectories {
        paths {
            path {
                setFrom("assets/")
                into = "${jib.container.appRoot}/assets"
            }
        }
    }
}
