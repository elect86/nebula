/*
 * Copyright 2013-2018 the original author or authors.
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
package nebula.test

import nebula.test.functional.ClasspathFilter
import nebula.test.functional.ExecutionResult
import nebula.test.functional.GradleRunnerFactory
import nebula.test.functional.PreExecutionAction
import nebula.test.functional.internal.GradleHandle
import nebula.test.multiproject.MultiProjectIntegrationHelper
import org.gradle.api.logging.LogLevel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import java.io.File
import kotlin.test.BeforeTest

import java.util.function.Predicate
import kotlin.reflect.KClass

/**
 * @author Justin Ryan
 * @author Marcin Erdmann
 */
abstract class Integration: IntegrationBase() {

    // Holds State of last run
    private var result: ExecutionResult? = null

    var gradleVersion: String? = null
    var settingsFile: File? = null
    var buildFile: File? = null
    var fork = false
    var remoteDebug = false
    var jvmArguments: List<String> = arrayListOf()
    lateinit var helper: MultiProjectIntegrationHelper
    var classpathFilter: ClasspathFilter? = null
    val preExecutionActions: ArrayList<PreExecutionAction> = arrayListOf()
    //Shutdown Gradle daemon after a few seconds to release memory. Useful for testing with multiple Gradle versions on shared CI server
    var memorySafeMode = false
    var daemonMaxIdleTimeInSecondsInMemorySafeMode = DEFAULT_DAEMON_MAX_IDLE_TIME_IN_SECONDS_IN_MEMORY_SAFE_MODE

    @BeforeEach
    open fun setup(testInfo: TestInfo) {
        // testInfo.displayName has ending `()`, so we use testMethod::name
        initialize(this::class, testInfo.testMethod.get().name)
    }

    override fun initialize(testClass: KClass<*>, testMethodName: String) {
        super.initialize(testClass, testMethodName)
        logLevel = LogLevel.INFO
        if (settingsFile == null) {
            settingsFile = projectDir / "settings.gradle"
            settingsFile!!.text = "rootProject.name='$moduleName'\n"
        }

        if (buildFile == null)
            buildFile = projectDir / "build.gradle"

        println("Running test from $projectDir")

        buildFile!!.appendText("// Running test for $moduleName\n")

        helper = MultiProjectIntegrationHelper(projectDir, settingsFile!!)
    }

    fun launcher(vararg args: String): GradleHandle {
        val arguments = calculateArguments(*args)
        val jvmArguments = calculateJvmArguments()
        val daemonMaxIdleTimeInSeconds = calculateMaxIdleDaemonTimeoutInSeconds()

        val runner = GradleRunnerFactory.createTooling(fork, gradleVersion, daemonMaxIdleTimeInSeconds, classpathFilter)
        return runner.handle(projectDir, arguments, jvmArguments, preExecutionActions)
    }

    private fun calculateJvmArguments(): List<String> =
        jvmArguments + if(remoteDebug) listOf(DEFAULT_REMOTE_DEBUG_JVM_ARGUMENTS) else emptyList()

    private fun calculateMaxIdleDaemonTimeoutInSeconds(): Int? =
        if(memorySafeMode) daemonMaxIdleTimeInSecondsInMemorySafeMode else null

    fun addInitScript(initFile: File) {
        initScripts += initFile
    }

    fun addPreExecute(preExecutionAction: PreExecutionAction) {
        preExecutionActions += preExecutionAction
    }

//    void copyResources(String srcDir, String destination) {
//        ClassLoader classLoader = getClass().getClassLoader()
//        URL resource = classLoader.getResource(srcDir)
//        if (resource == null) {
//            throw new RuntimeException("Could not find classpath resource: $srcDir")
//        }
//
//        File destinationFile = file(destination)
//        File resourceFile = new File(resource.toURI())
//        if (resourceFile.file) {
//            GFileUtils.copyFile(resourceFile, destinationFile)
//        } else {
//            GFileUtils.copyDirectory(resourceFile, destinationFile)
//        }
//    }
//
//    String applyPlugin(Class pluginClass) {
//        "apply plugin: $pluginClass.name"
//    }

    /* Checks */
    fun fileExists(path: String): Boolean = projectDir.resolve(path).exists()

    /* Execution */
    fun runTasksSuccessfully(vararg tasks: String): ExecutionResult {
        val result = runTasks(*tasks)
        if (result.failure != null)
            result.rethrowFailure()
        return result
    }

//    @CompileStatic(TypeCheckingMode.SKIP)
    fun runTasksWithFailure(vararg tasks: String): ExecutionResult {
        val result = runTasks(*tasks)
        assert(result.failure != null)
        return result
    }

    fun runTasks(vararg tasks: String): ExecutionResult {
        val gradleHandle = launcher(*tasks)
        val result = gradleHandle.run()
        this.result = result
        gradleHandle.disconnect()
        checkOutput(result.standardOutput)
        checkOutput(result.standardError)
        return result
    }

    infix fun addSubproject(subprojectName: String): File = helper addSubproject subprojectName

    fun addSubproject(subprojectName: String, subBuildGradleText: String): File =
        helper.addSubproject(subprojectName, subBuildGradleText)

    companion object {
        private const val DEFAULT_REMOTE_DEBUG_JVM_ARGUMENTS = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
        private val DEFAULT_DAEMON_MAX_IDLE_TIME_IN_SECONDS_IN_MEMORY_SAFE_MODE = 15
    }
}
