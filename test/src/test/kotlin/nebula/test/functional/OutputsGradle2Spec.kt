package nebula.test.functional

import nebula.test.div
import nebula.test.plusAssign
import nebula.test.text
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test


// issue https://github.com/nebula-plugins/nebula-test/issues/29
class OutputsGradle2Spec {

    @TempDir
    lateinit var tmp: File

    @Test
    fun `println included in standardOutput in fork mode`() {

        val runner = GradleRunnerFactory.createTooling(FORK_MODE)
        val build = tmp / "build.gradle"
        build.createNewFile()
        build += "apply plugin: ${SomePlugin::class.qualifiedName}"

        val result = runner.run(tmp, listOf("print"))

        assert("Printed (stdout)" in result.standardOutput)
    }

    @Test
    fun `err․println included in standardError or standardOutput in fork mode`() {

        val runner = GradleRunnerFactory.createTooling(FORK_MODE)
        val build = tmp / "build.gradle"
        build.createNewFile()
        build += "apply plugin: ${SomePlugin::class.qualifiedName}"

        val result = runner.run(tmp, listOf("print"))

        val expectedMessage = "Printed (stderr)"
        //Gradle 4.7 started to print error log messages into standard output
        //we run build with version lower than 4.7 as well higher so we check both places
        assert(expectedMessage in result.standardError || expectedMessage in result.standardOutput)
    }

    @Test
    fun `stdout redirected to WARN included in standardOutput in fork mode`() {

        val runner = GradleRunnerFactory.createTooling(FORK_MODE)
        val build = tmp / "build.gradle"
        build.createNewFile()
        build += """
            |logging.captureStandardOutput LogLevel.WARN
            |apply plugin: ${SomePlugin::class.qualifiedName}""".trimMargin()

        val result = runner.run(tmp, listOf("print"))

        assert("Printed (stdout)" in result.standardOutput)
    }

    @Test
    fun `stdout redirected to ignored logging level not included in standardOutput in fork mode`() {

        val runner = GradleRunnerFactory.createTooling(FORK_MODE)
        val build = tmp / "build.gradle"
        build.createNewFile()
        build += """
            |logging.captureStandardOutput LogLevel.TRACE
            |apply plugin: ${SomePlugin::class.qualifiedName}""".trimMargin()

        val result = runner.run(tmp, listOf("print"))

        assert("Printed (stdout)" !in result.standardOutput)
    }

    companion object {
        private const val FORK_MODE = true
    }
}
