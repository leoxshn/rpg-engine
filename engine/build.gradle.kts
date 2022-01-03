import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.kotlinVersion
    `maven-publish`
}

group = "io.posidon"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.posidon:uranium.mathlib:-SNAPSHOT")

    implementation(platform(Dependencies.lwjgl("bom")))
    implementation(Dependencies.lwjgl())
    implementation(Dependencies.lwjgl("glfw"))
    implementation(Dependencies.lwjgl("opengl"))
    implementation(Dependencies.lwjgl("stb"))
    
    runtimeOnly(Dependencies.lwjglNatives())
    runtimeOnly(Dependencies.lwjglNatives("glfw"))
    runtimeOnly(Dependencies.lwjglNatives("opengl"))
    runtimeOnly(Dependencies.lwjglNatives("stb"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinVersion}-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.posidon"
            artifactId = "uranium.engine"
            version = "1.0.0"

            from(components["kotlin"])
        }
    }
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}