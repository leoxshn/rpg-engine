import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
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
    implementation(project(":engine"))
    //implementation("io.posidon:uranium.mathlib:mathlib-jvm:02024afad6")

    runtimeOnly(Dependencies.lwjglNatives())
    runtimeOnly(Dependencies.lwjglNatives("glfw"))
    runtimeOnly(Dependencies.lwjglNatives("opengl"))
    runtimeOnly(Dependencies.lwjglNatives("stb"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "io.posidon.rpg.GameKt"
    }
    // To add all of the dependencies otherwise a "NoClassDefFoundError" error
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({ configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) } })
}