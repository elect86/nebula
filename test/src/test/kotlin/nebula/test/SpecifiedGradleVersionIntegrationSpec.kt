/*
 * Copyright 2017 Netflix, Inc.
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
package nebula.test

import org.junit.jupiter.api.condition.DisabledIf
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

class SpecifiedGradleVersionIntegrationSpec: Integration() {

    fun setup() {
        fork = true
    }

//    TODO
//    @IgnoreIf({ OperatingSystem.current.linux || OperatingSystem.current.macOs || jvm.isJava9Compatible()})
//    @Unroll("should use Gradle #requestedGradleVersion when requested ")
//    def "should allow to run functional tests with different Gradle versions Windows"() {
//        given:
//            writeHelloWorld('nebula.test.hello')
//            buildFile << '''
//                apply plugin: 'java'
//
//                task showVersion {
//                    doLast {
//                        println "Gradle Version: ${gradle.gradleVersion}"
//                    }
//                }
//            '''.stripIndent()
//
//        and:
//            gradleVersion = requestedGradleVersion
//
//        when:
//            def result = runTasksSuccessfully('showVersion')
//
//        then:
//
//        result.standardOutput.contains("Gradle Version: $requestedGradleVersion")
//
//        where:
//            requestedGradleVersion << ['7.0-milestone-2']
//    }

//    @IgnoreIf({ OperatingSystem.current.windows || jvm.isJava9Compatible() })
    @EnabledOnOs(OS.LINUX, OS.MAC)
    @DisabledIf("isJava9Compatible")
//    @Unroll("should use Gradle #requestedGradleVersion when requested ")
    @ParameterizedTest
    @ValueSource(strings = ["7.0-milestone-2"])
    fun `should allow to run functional tests with different Gradle versions Linux - Mac`(requestedGradleVersion: String) {

        writeHelloWorld("nebula.test.hello")
        buildFile!! += """
            |apply plugin: 'java'
            |
            |task showVersion {
            |   doLast {
            |       println "Gradle Version: $\{gradle.gradleVersion}"
            |   }
            |}""".trimMargin()

        gradleVersion = requestedGradleVersion

        val result = runTasksSuccessfully("showVersion")

        assert("Gradle Version: $requestedGradleVersion" in result.standardOutput)
    }

//    static final String CUSTOM_DISTRIBUTION = 'https://dl.bintray.com/nebula/gradle-distributions/1.12-20140608201532+0000/gradle-1.12-20140608201532+0000-bin.zip'
//
//    @Ignore("Only works with a custom distribution that is compatible with our runtime, of which 1.12 is not compatible with our spock 2.0 dependency")
//    def 'should be able to use custom distribution'() {
//        buildFile << '''
//                task showVersion << {
//                   println "Gradle Version: ${gradle.gradleVersion}"
//                }
//            '''.stripIndent()
//        File wrapperProperties = new File(projectDir, 'gradle/wrapper/gradle-wrapper.properties')
//        wrapperProperties.parentFile.mkdirs()
//        wrapperProperties << """
//            #Tue Jun 03 14:28:56 PDT 2014
//            distributionBase=GRADLE_USER_HOME
//            distributionPath=wrapper/dists
//            zipStoreBase=GRADLE_USER_HOME
//            zipStorePath=wrapper/dists
//            distributionUrl=${CUSTOM_DISTRIBUTION}
//        """
//
//        when:
//        def result = runTasksSuccessfully('showVersion')
//
//        then:
//        result.standardOutput.contains("Gradle Version: 1.12-20140608201532+0000")
//    }
//
//    @Ignore("Only works with a custom distribution that is compatible with our runtime, of which 1.12 is not compatible with our spock 2.0 dependency")
//    def 'should be able to use custom distribution in a test'() {
//        def testFile = new File(projectDir, "src/test/groovy/testing/DistributionTest.groovy")
//        testFile.parentFile.mkdirs()
//        testFile << '''
//            package testing
//
//            import nebula.test.IntegrationSpec
//            import nebula.test.functional.ExecutionResult
//
//            class DistributionTest extends IntegrationSpec {
//                def 'confirm distribution'() {
//                    buildFile << """
//                        task print << {
//                            println "Gradle Inner Test Version: \\${gradle.gradleVersion}"
//                        }
//                    """.stripIndent()
//                    expect:
//                    runTasksSuccessfully('print')
//                }
//            }
//            '''.stripIndent()
//        buildFile << '''
//            apply plugin: 'groovy'
//            dependencies {
//                testCompile localGroovy()
//            }
//            sourceSets.test.compileClasspath += [buildscript.configurations.classpath]
//            test {
//                classpath += [buildscript.configurations.classpath]
//                testLogging {
//                    events "passed", "skipped", "failed", "standardOut", "standardError"
//                }
//            }
//            '''.stripIndent()
//        writeHelloWorld('testing')
//
//        File wrapperProperties = new File(projectDir, 'gradle/wrapper/gradle-wrapper.properties')
//        wrapperProperties.parentFile.mkdirs()
//        wrapperProperties << """
//            #Tue Jun 03 14:28:56 PDT 2014
//            distributionBase=GRADLE_USER_HOME
//            distributionPath=wrapper/dists
//            zipStoreBase=GRADLE_USER_HOME
//            zipStorePath=wrapper/dists
//            distributionUrl=${CUSTOM_DISTRIBUTION}
//        """.stripIndent()
//
//        when:
//        def result = runTasksSuccessfully('test')
//
//        then:
//        result.standardOutput.contains("Gradle Inner Test Version: 1.12-20140608201532+0000")
//    }

    companion object {
        @JvmStatic
        fun isJava9Compatible() = org.gradle.api.JavaVersion.current().isJava9Compatible
    }
}
