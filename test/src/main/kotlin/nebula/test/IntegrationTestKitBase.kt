/*
 * Copyright 2016-2018 Netflix, Inc.
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

import nebula.test.functional.GradleRunnerFactory
import nebula.test.functional.internal.classpath.ClasspathAddingInitScriptBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import java.io.File
import java.io.PrintWriter
import java.lang.management.ManagementFactory
import java.net.URI
import kotlin.reflect.KClass

/**
 * Base trait for implementing gradle integration tests using the {@code gradle-test-kit} runner.
 */
abstract class IntegrationTestKitBase : IntegrationBase() {

    var keepFiles = false
    var debug = false
    lateinit var buildFile: File
    var settingsFile: File? = null
    var gradleVersion: String? = null
    var gradleDistribution: String? = null
    var forwardOutput = false

    /**
     * Automatic addition of `GradleRunner.withPluginClasspath()` _only_ works if the plugin under test is applied using the plugins DSL
     * This enables us to add the plugin-under-test classpath via an init script
     * https://docs.gradle.org/4.6/userguide/test_kit.html#sub:test-kit-automatic-classpath-injection
     */
    var definePluginOutsideOfPluginBlock = false

    override fun initialize(testClass: KClass<*>, testMethodName: String) = initialize(testClass, testMethodName, "nebulatest")

    override fun initialize(testClass: KClass<*>, testMethodName: String, baseFolderName: String) {
        super.initialize(testClass, testMethodName, baseFolderName)
        if (settingsFile == null)
            settingsFile = File(projectDir, "settings.gradle").apply { text = "rootProject.name='$moduleName'\n" }
        buildFile = projectDir / "build.gradle"
    }

    fun traitCleanup() {
        if (!keepFiles)
            projectDir.delete()
    }

    fun addSubproject(name: String): File {
        val subprojectDir = projectDir / name
        subprojectDir.mkdirs()
        settingsFile!!.appendText("include \"$name\"$LINE_END")
        return subprojectDir
    }

    fun addSubproject(name: String, buildGradle: String): File {
        val subdir = addSubproject(name)
        File(subdir, "build.gradle").text = buildGradle
        return subdir
    }

    fun runTasks(vararg tasks: String): BuildResult {
        val result = createRunner(*tasks).build()
        checkOutput(result.output)
        return result
    }

    fun runTasksAndFail(vararg tasks: String): BuildResult {
        val result = createRunner(*tasks).buildAndFail()
        checkOutput(result.output)
        return result
    }

    fun tasksWereSuccessful(result: BuildResult, vararg tasks: String) {
        for (task in tasks)
            if ("-P" !in task && "--" !in task) {
                val modTask = if (task.startsWith(':')) task else ":$task"
                val outcome = result.task(modTask)?.outcome
                assert(outcome == SUCCESS || outcome == UP_TO_DATE)
            }
    }

    fun createRunner(vararg tasks: String): GradleRunner {
        val pluginArgs = if (definePluginOutsideOfPluginBlock) createGradleTestKitInitArgs() else emptyList()
        debug = if (debug) true else isJwdpLoaded
        val gradleRunnerBuilder = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(pluginArgs + calculateArguments(*tasks))
                .withDebug(debug)
                .withPluginClasspath()

        gradleRunnerBuilder.forwardStdError(PrintWriter(System.err))
        if (forwardOutput)
            gradleRunnerBuilder.forwardStdOutput(PrintWriter(System.out))
        gradleVersion?.let(gradleRunnerBuilder::withGradleVersion)
        gradleDistribution?.let { gradleRunnerBuilder.withGradleDistribution(URI.create(it)) }
        return gradleRunnerBuilder
    }

    private fun createGradleTestKitInitArgs(): List<String> {
        val testKitDir = projectDir / ".gradle-test-kit"
        if (!testKitDir.exists())
            testKitDir.mkdirs()

        val initScript = testKitDir / "init.gradle"
        val classLoader = this::class.java.classLoader
        val classpathFilter = GradleRunnerFactory.CLASSPATH_DEFAULT
        ClasspathAddingInitScriptBuilder.build(initScript, classLoader, classpathFilter)

        return listOf("--init-script", initScript.absolutePath)
    }

    companion object {
        val LINE_END = System.lineSeparator()

        val isJwdpLoaded: Boolean
            get() {
                val runtime = ManagementFactory.getRuntimeMXBean()
                val args = runtime.inputArguments
                return "-agentlib:jdwp" in args.toString()
            }
    }
}
