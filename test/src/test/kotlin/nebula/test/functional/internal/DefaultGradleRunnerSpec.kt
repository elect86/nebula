package nebula.test.functional.internal

import nebula.test.functional.ExecutionResult
import nebula.test.functional.PreExecutionAction
import nebula.test.functional.internal.DefaultGradleRunner
import nebula.test.functional.internal.GradleHandleFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test

class DefaultGradleRunnerSpec {

    @TempDir
    lateinit var temporaryFolder: File

    @Test
    fun `will execute actions before run is called`() {

        val projectDir = temporaryFolder
        val handleFactory = object : GradleHandleFactory {
            override fun start(dir: File, arguments: List<String>): GradleHandle = start(dir, arguments, emptyList())
            override fun start(dir: File, arguments: List<String>, jvmArguments: List<String>): GradleHandle =
                object : GradleHandle {
                    override fun run(): ExecutionResult {; TODO("Not yet implemented"); }
                    override fun registerBuildListener(buildListener: GradleHandleBuildListener) {; TODO("Not yet implemented"); }
                    override val forkedProcess: Boolean get() = TODO("Not yet implemented")
                    override fun disconnect() {; TODO("Not yet implemented"); }
                }
        }
        val runner = DefaultGradleRunner(handleFactory)

        runner.handle(projectDir, listOf("arg"), listOf("jvm"), listOf(WriteFileAction(projectDir)))

        handleFactory.start(projectDir, listOf("arg"), listOf("jvm"))
    }

    class WriteFileAction(val expectedDir: File) : PreExecutionAction {

        override fun invoke(projectDir: File, arguments: List<String>, jvmArguments: List<String>) {

            assert(expectedDir.absolutePath == projectDir.absolutePath)

            assert(arguments.size == 1)
            assert(arguments.first() == "arg")

            assert(jvmArguments.size == 1)
            assert(jvmArguments.first() == "jvm")
        }
    }
}
