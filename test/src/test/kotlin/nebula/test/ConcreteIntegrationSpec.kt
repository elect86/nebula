package nebula.test

import nebula.test.functional.ExecutionResult
import nebula.test.functional.PreExecutionAction
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

class ConcreteIntegrationSpec: BaseIntegrationSpec() {

    @Test
    fun `runs build`() {

        val buildResult = runTasks("dependencies")

        assert(buildResult.failure == null)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `setup and run build for #type execution`(forked: Boolean) {

        buildFile!! += "apply plugin: 'java'"
        fork = forked

        writeHelloWorld("nebula.test.hello")

        assert(fileExists ("src/main/java/nebula/test/hello/HelloWorld.java"))

        val result = runTasksSuccessfully("build", "--info")

        assert(fileExists ("build/classes/java/main/nebula/test/hello/HelloWorld.class"))
        assert(result.wasExecuted (":compileTestJava"))
        assert("Skipping task \':compileTestJava\' as it has no source files and no previous output files." in result.standardOutput)

//        where:
//        type         | forked
//        'in-process' | false
//        'forked'     | true
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `can import from classpath using #desc #testTooling`(/*String desc, boolean testTooling*/) {
        buildFile!! += """
            import nebula.test.FakePlugin
            apply plugin: FakePlugin
        """.trimIndent()

        runTasksSuccessfully("tasks")

//        noExceptionThrown()

//        where:
//        desc       | testTooling
//        "Tooling"  | true
//        "Launcher" | false
    }

    @Test
    fun `init scripts will be appended to arguments provided to gradle`() {

        val initScript = file("foo.gradle")
        initScript.text = """
            gradle.projectsLoaded {
                gradle.rootProject.tasks.create('foo')
            }
        """.trimIndent()

        var failure = runTasksWithFailure("foo")

        assert(failure.failure != null)

        addInitScript(initScript)
        failure = runTasksSuccessfully("foo")
        assert(failure.failure == null)

//        then:
//        noExceptionThrown()
    }

    @Test
    fun `pre execution tasks will run before gradle`() {

        val initScript = file("foo.gradle")

        addPreExecute { _, _, _ ->
            initScript.text = """
                gradle.projectsLoaded {
                    gradle.rootProject.tasks.create('foo')
                }
            """.trimIndent()
        }
        addInitScript(initScript)
        runTasksSuccessfully("foo")

//        then:
//        noExceptionThrown()
    }
}
