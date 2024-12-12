/**
 *
 *  Copyright 2020 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package nebula.test

import nebula.test.dependencies.DependencyGraphBuilder
import nebula.test.dependencies.GradleDependencyGenerator
import nebula.test.dependencies.ModuleBuilder
import kotlin.test.Test

class IntegrationTestKitSpecSpec : IntegrationTestKitSpec() {

    //    def setup() {
    //        // used to test trait & groovy setup method https://stackoverflow.com/questions/56464191/public-groovy-method-must-be-public-says-the-compiler
    //    }
    //
    //    def cleanup() {
    //        // used to test trait & groovy cleanup method https://stackoverflow.com/questions/56464191/public-groovy-method-must-be-public-says-the-compiler
    //    }

    @Test
    fun `dependencies method should list buildfile dependencies`() {

        val graph = DependencyGraphBuilder()
                .addModule("testjava:a:0.1.0")
                .addModule(ModuleBuilder("testjava:b:0.1.0")
                                   .addDependency("testjava:a:0.1.0").build())
                .addModule("testjava:c:0.1.0").build()
        val mavenrepo = GradleDependencyGenerator(graph, "$projectDir/testrepogen").generateTestMavenRepo()

        buildFile += """
            apply plugin 'java-library'
            repositories {
                maven { url '${mavenrepo.absolutePath}' }
            }
            dependencies {
                implementation 'testjava:a:0.1.0'
                api 'testjava:b:0.1.0'
                testImplementation 'testjava:c:0.1.0'
            }
            """.trimIndent()

        assert(dependencies(buildFile, "implementation") == listOf("testjava:a:0.1.0"))
        assert(dependencies(buildFile, "api") == listOf("testjava:b:0.1.0"))
        assert(dependencies(buildFile, "testImplementation") == listOf("testjava:c:0.1.0"))
        assert(dependencies(buildFile) == listOf("testjava:a:0.1.0", "testjava:b:0.1.0", "testjava:c:0.1.0"))
    }
}
