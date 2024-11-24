/*
 * Copyright 2015-2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.dependencies.comparison

import kotlin.test.Test

class DependenciesComparisonSpec {

    @Test
    fun `should diff single project no skew between configurations`() {
        val old = ConfigurationsSet("compileClasspath" to Dependencies("test.nebula:a" to "1.0.0"),
                                    "runtimeClasspath" to Dependencies("test.nebula:a" to "1.0.0"))
        val updated = ConfigurationsSet("compileClasspath" to Dependencies("test.nebula:a" to "1.1.0"),
                                        "runtimeClasspath" to Dependencies("test.nebula:a" to "1.1.0"))

        val result = DependenciesComparison.performDiff(old, updated)

        assert(result.size == 1)
        val diff = result.first()
        //        diff.updated TODO
        assert(diff.updatedDiffString == "  test.nebula:a: 1.0.0 -> 1.1.0")
    }

    @Test
    fun `should handle new dependency`() {
        val old = ConfigurationsSet("compileClasspath" to Dependencies(),
                                    "runtimeClasspath" to Dependencies())
        val updated = ConfigurationsSet("compileClasspath" to Dependencies("test.nebula:a" to "1.0.0"),
                                        "runtimeClasspath" to Dependencies("test.nebula:a" to "1.0.0"))

        val result = DependenciesComparison.performDiff(old, updated)

        assert(result.size == 1)
        val diff = result.first()
        //        diff.new TODO
        assert(diff.newDiffString == "  test.nebula:a: 1.0.0")
    }

    @Test
    fun `should handle removed dependency`() {

        val old = ConfigurationsSet("compileClasspath" to Dependencies("test.nebula:a" to "1.0.0"),
                                    "runtimeClasspath" to Dependencies("test.nebula:a" to "1.0.0"))
        val updated = ConfigurationsSet("compileClasspath" to Dependencies(),
                                        "runtimeClasspath" to Dependencies())

        val result = DependenciesComparison.performDiff(old, updated)

        assert(result.size == 1)
        val diff = result.first()
        //        diff.removed TODO
        assert(diff.removedDiffString == "  test.nebula:a")
    }

    fun `should handle multiple configurations`() {

        val old = ConfigurationsSet("compileClasspath" to Dependencies("test.nebula:a" to "1.0.0"),
                                    "runtimeClasspath" to Dependencies("test.nebula:a" to "1.0.0"),
                                    "testCompileClasspath" to Dependencies("test.nebula:a" to "1.0.0", "test.nebula:testlib" to "2.0.0"),
                                    "testRuntimeClasspath" to Dependencies("test.nebula:a" to "1.0.0", "test.nebula:testlib" to "2.0.0"))
        val updated = ConfigurationsSet("compileClasspath" to Dependencies("test.nebula:a" to "1.1.0"),
                                        "runtimeClasspath" to Dependencies("test.nebula:a" to "1.1.0"),
                                        "testCompileClasspath" to Dependencies("test.nebula:a" to "1.1.0", "test.nebula:testlib" to "2.0.2"),
                                        "testRuntimeClasspath" to Dependencies("test.nebula:a" to "1.1.0", "test.nebula:testlib" to "2.0.2"))

        val result = DependenciesComparison.performDiff(old, updated)

        assert(result.size == 2)
        //        result[0].updated TODO
        assert(result[0].updatedDiffString == "  test.nebula:a: 1.0.0 -> 1.1.0")
        //        result[1].updated
        assert(result[1].updatedDiffString == "  test.nebula:testlib: 2.0.0 -> 2.0.2")
    }

    @Test
    fun `should handle inconsistent configurations`() {

        val old = ConfigurationsSet(
            "compileClasspath" to Dependencies("test.nebula:a" to "1.0.0"),
            "runtimeClasspath" to Dependencies("test.nebula:a" to "1.0.0"),
            "testCompileClasspath" to Dependencies("test.nebula:a" to "1.0.0"),
            "testRuntimeClasspath" to Dependencies("test.nebula:a" to "1.0.0"))
        val updated = ConfigurationsSet("compileClasspath" to Dependencies("test.nebula:a" to "1.1.0"),
                                        "runtimeClasspath" to Dependencies("test.nebula:a" to "1.1.0"),
                                        "testCompileClasspath" to Dependencies("test.nebula:a" to "1.1.1"),
                                        "testRuntimeClasspath" to Dependencies("test.nebula:a" to "1.1.1"))

        val result = DependenciesComparison.performDiff(old, updated)

        assert(result.size == 1)
        val diff = result.first()
        //        diff.inconsistent TODO
        val inconsistent = diff.inconsistentDiffList
        assert(inconsistent[0] == "  test.nebula:a:")
        assert(inconsistent[1] == "    1.0.0 -> 1.1.0 [compileClasspath,runtimeClasspath]")
        assert(inconsistent[2] == "    1.0.0 -> 1.1.1 [testCompileClasspath,testRuntimeClasspath]")
    }
}
