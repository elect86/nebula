package nebula.test.functional.internal.classpath

import nebula.test.Integration
import nebula.test.div
import kotlin.test.Test


class ClasspathAddingInitScriptBuilderFunctionalTest : Integration() {

    @Test
    fun `can use generated init script with huge amount of dependencies`() {

        val initScript = projectDir / "build/init.gradle"
        val libs = ClasspathAddingInitScriptBuilderFixture.createLibraries(projectDir)
        ClasspathAddingInitScriptBuilder.build(initScript, libs)

        buildFile!!.appendText("""
            |task helloWorld {
            |    doLast {
            |        logger.quiet 'Hello World!'
            |    }
            |}
            """.trimMargin())

        val executionResult = runTasksSuccessfully("helloWorld", "--init-script", initScript.canonicalPath)

        assert("Hello World!" in executionResult.standardOutput)
    }
}
