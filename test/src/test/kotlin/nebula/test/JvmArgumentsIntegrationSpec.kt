package nebula.test

import org.junit.jupiter.api.Test

class JvmArgumentsIntegrationSpec : Integration() {

    @Test
    fun `should start Gradle with custom JVM argument in fork mode`() {

        writeHelloWorld("nebula.test.hello")
        buildFile!! += "apply plugin: 'java'"

        fork = true
        jvmArguments = listOf(TEST_JVM_ARGUMENT)

        val result = runTasksSuccessfully("compileJava", "--debug")
        assert(TEST_JVM_ARGUMENT in result.standardOutput)
    }

    companion object {
        private const val TEST_JVM_ARGUMENT = "-XX:-PrintClassHistogram"
    }
}
