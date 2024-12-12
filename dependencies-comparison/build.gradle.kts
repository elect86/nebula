import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    //    id "nebula.netflixoss" version "10.3.0"
    embeddedKotlin("jvm")
}

repositories {
    mavenCentral()
}

description = "Library for comparing dependencies in configurations"


dependencies {
    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<JavaCompile>().configureEach { targetCompatibility = "1.8" }
    withType<KotlinCompile>().configureEach { compilerOptions { jvmTarget = JvmTarget.JVM_1_8 } }
}

configurations {
    compileClasspath {
        resolutionStrategy.activateDependencyLocking()
    }
}