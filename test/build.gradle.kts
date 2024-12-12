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
//    implementation(kotlin("stdlib"))
    implementation(kotlin("test"))
    implementation(kotlin("test-junit5"))
//    implementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.20")
    implementation(gradleTestKit())

    // https://mvnrepository.com/artifact/uk.org.webcompere/system-stubs-jupiter
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.1.7")
    // https://mvnrepository.com/artifact/org.hamcrest/hamcrest
    testImplementation("org.hamcrest:hamcrest:3.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
}

tasks {
    test {
        useJUnitPlatform()
        // this is needed for `ClasspathAddingInitScriptBuilderIntegrationTest.can build init script with huge`
        jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    }

    withType<JavaCompile>().configureEach { targetCompatibility = "1.8" }
    withType<KotlinCompile>().configureEach { compilerOptions { jvmTarget = JvmTarget.JVM_1_8 } }
}

configurations {
    compileClasspath {
        resolutionStrategy.activateDependencyLocking()
    }
}