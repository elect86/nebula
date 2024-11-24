plugins {
//    id "nebula.netflixoss" version "10.3.0"
    embeddedKotlin("jvm")
}

repositories {
    mavenCentral()
}

//targetCompatibility = 1.8

description = "Library for comparing dependencies in configurations"


dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}