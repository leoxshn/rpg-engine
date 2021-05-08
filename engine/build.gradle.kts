import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.kotlinVersion
}

group = "io.posidon"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":shared"))

    implementation(platform(Dependencies.lwjgl("bom")))
    implementation(Dependencies.lwjgl())
    implementation(Dependencies.lwjgl("glfw"))
    implementation(Dependencies.lwjgl("opengl"))
    implementation(Dependencies.lwjgl("stb"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinVersion}-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}