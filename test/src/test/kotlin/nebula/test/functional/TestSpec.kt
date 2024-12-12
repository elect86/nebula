/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nebula.test.functional

import nebula.test.div
import nebula.test.plusAssign
import nebula.test.text
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.test.Test


class TestSpec {

    @TempDir
    lateinit var tmp: File

    @ParameterizedTest/*(name = "for {arguments} GradleRunner")*/
    //@FieldSource("simpleArguments") // only since 5.11, we are on 5.10.1
    @ValueSource(booleans = [false, true])
    fun `Check up-to-date and skipped task states for #type GradleRunner`(forked: Boolean) {

        val runner = GradleRunnerFactory.createTooling(forked)
        val build = tmp / "build.gradle"
        build.createNewFile()
        build += "apply plugin: ${SomePlugin::class.qualifiedName}"

        var result = runner.run(tmp, listOf("echo", "doIt", "-PupToDate=false", "-Pskip=false"))
        assert(!result.wasExecuted(":hush"))
        assert(result.wasExecuted(":echo"))
        assert(!result.wasUpToDate(":echo"))
        assert(result.wasExecuted(":doIt"))
        assert(!result.wasSkipped(":doIt"))

        assert("I ran!" in result.standardOutput)
        assert("Did it!" in result.standardOutput)

        result = runner.run(tmp, listOf("echo", "doIt", "-PupToDate=true", "-Pskip=true"))

        assert("I ran!" !in result.standardOutput)
        assert("Did it!" !in result.standardOutput)
        assert(result.wasExecuted(":echo"))
        assert(result.wasUpToDate(":echo"))
        assert(result.wasExecuted(":doIt"))
        assert(result.wasSkipped(":doIt"))

        //        where:
        //        type         | forked
        //        'in-process' | false
        //        'forked'     | true
    }

    @ParameterizedTest
    //@FieldSource("simpleArguments") // only since 5.11, we are on 5.10.1
    @ValueSource(booleans = [false, true])
    fun `Task path doesn't need to start with colon for #type GradleRunner`(forked: Boolean) {

        val runner = GradleRunnerFactory.createTooling(forked)
        val build = tmp / "build.gradle"
        build.createNewFile()
        build += "apply plugin: ${SomePlugin::class.qualifiedName}"

        val result = runner.run(tmp, listOf("echo", "doIt"))
        assert( result.wasExecuted ("echo"))
        assert(!result.wasUpToDate("echo"))
        assert(!result.wasSkipped("doIt"))

        assert("I ran!" in result.standardOutput)
        assert("Did it!" in result.standardOutput)

//        where:
//        type         | forked
//        'in-process' | false
//        'forked'     | true
    }

    companion object {
//        private val simpleArguments: List<Arguments> = listOf(arguments("in-process"), arguments("forked"))
    }
}
