/*
 * Copyright 2014-2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.gradle.util.GradleVersion
import kotlin.math.absoluteValue
import kotlin.math.sign

plugins {
    //    id 'com.netflix.nebula.plugin-plugin' version '21.2.0'
    //    id 'com.netflix.nebula.optional-base' version '9.0.0'
    embeddedKotlin("jvm")
    `java-gradle-plugin`
    //    id "org.gradle.test-retry" version "1.5.0"
}

description = "Gradle plugin to allow locking of dynamic dependency versions"

group = "com.netflix.nebula"

repositories {
    mavenCentral()
}

//contacts {
//    'nebula-plugins-oss@netflix.com' {
//        moniker 'Nebula Plugins Maintainers'
//        github 'nebula-plugins'
//    }
//}

dependencies {
    //    implementation 'com.squareup.moshi:moshi:1.12.+'
    //    implementation 'org.apache.commons:commons-lang3:3.12.0'
    //    implementation 'joda-time:joda-time:2.10'
    //    implementation 'com.netflix.nebula:nebula-gradle-interop:latest.release'
    implementation(projects.dependenciesComparison)

    implementation(platform("com.fasterxml.jackson:jackson-bom:2.14.2"))

    //    testImplementation 'com.netflix.nebula:nebula-project-plugin:latest.release'
    //    testImplementation 'com.netflix.nebula:nebula-test:latest.release'
    testImplementation(gradleTestKit())
    //    testImplementation('org.ajoberstar.grgit:grgit-core:4.1.1') {
    //        exclude group: 'org.codehaus.groovy', module: 'groovy'
    //    }
    //    testImplementation "com.github.tomakehurst:wiremock:2.17.0"
    //    testImplementation 'com.github.stefanbirkner:system-rules:1.19.0'
    //    testImplementation 'org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r'
}

//configurations.all {
//    resolutionStrategy {
//        force 'org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r'
//    }
//}
gradlePlugin {
    plugins.create("dependencyLock") {
        id = "com.netflix.nebula.dependency-lock"
        displayName = "Nebula Dependency Lock plugin"
        implementationClass = "nebula.plugin.dependencylock.DependencyLockPlugin"
        description = "Plugin to lock dynamic dependencies"
        tags = listOf("nebula", "dependencies", "lock")
    }
}

//tasks.named('compileGroovy') {
//    // Groovy only needs the declared dependencies
//    // and not the output of compileJava
//    classpath = sourceSets.main.compileClasspath
//}
//tasks.named('compileKotlin') {
//    // Kotlin also depends on the result of Groovy compilation
//    // which automatically makes it depend of compileGroovy
//    libraries.from(files(sourceSets.main.groovy.classesDirectory))
//}
//
//test.dependsOn jar

tasks{
    test.configure {
        maxParallelForks = Runtime.getRuntime().availableProcessors() ceilDiv 2
        testLogging {
            events("PASSED", "FAILED", "SKIPPED")
//            afterSuite { desc, result ->
//                if (!desc.parent) { // will match the outermost suite
//                    def output = "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
//                    def startItem = '|  ', endItem = '  |'
//                    def repeatLength = startItem . length () + output.length() + endItem.length()
//                    println('\n' + ('-' * repeatLength) + '\n' + startItem + output + endItem + '\n' + ('-' * repeatLength))
//                }
//            }
        }
//        retry {
//            maxRetries = 3
//            maxFailures = 20
//        }
    }
    check {
        // Include functionalTest as part of the check lifecycle
        dependsOn(testing.suites.named("functionalTest"))
    }
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest(embeddedKotlinVersion)
        }

        // Create a new test suite
        val functionalTest by registering(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest(embeddedKotlinVersion)

            dependencies {
                // functionalTest test suite depends on the production code in tests
                implementation(project())
            }

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure { shouldRunAfter(test) }
                }
            }
        }
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

//java {
//    sourceCompatibility = JavaVersion.VERSION_1_8
//    targetCompatibility = JavaVersion.VERSION_1_8
//}
//
//idea {
//    project {
//        jdkName = '1.8'
//        languageLevel = '1.8'
//    }
//}
//
//tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).all {
//    kotlinOptions {
//        jvmTarget = '1.8'
//    }
//}
//
//tasks.withType(GenerateModuleMetadata).configureEach {
//    suppressedValidationErrors.add('enforced-platform')
//}
//
//javaCrossCompile {
//    disableKotlinSupport = true
//}

/* Divides this value by the other value, ceiling the result to an integer that is closer to positive infinity. */
infix fun Int.ceilDiv(other: Int) = floorDiv(other) + rem(other).sign.absoluteValue