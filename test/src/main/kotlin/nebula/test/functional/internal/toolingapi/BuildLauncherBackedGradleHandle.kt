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

package nebula.test.functional.internal.toolingapi

import nebula.test.functional.ExecutionResult
import nebula.test.functional.internal.GradleHandle
import nebula.test.functional.internal.GradleHandleBuildListener
import org.gradle.tooling.BuildException
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.events.ProgressEvent
import org.gradle.tooling.events.task.TaskOperationDescriptor
import java.io.ByteArrayOutputStream

class BuildLauncherBackedGradleHandle(val connector: GradleConnector,
                                      val launcher: BuildLauncher,
                                      override val forkedProcess: Boolean) : GradleHandle {

    private val sout = ByteArrayOutputStream()
    private val serr = ByteArrayOutputStream()
    private val tasksExecuted: ArrayList<String> = arrayListOf()
    private var buildListener: GradleHandleBuildListener? = null

    init {
        launcher.setStandardOutput(sout)
        launcher.setStandardError(serr)

        launcher.addProgressListener { event: ProgressEvent ->
            if (event.descriptor is TaskOperationDescriptor) {
                val descriptor = event.descriptor as TaskOperationDescriptor
                tasksExecuted += descriptor.taskPath
            }
        }
    }

    override fun registerBuildListener(buildListener: GradleHandleBuildListener) {
        this.buildListener = buildListener
    }

    override fun disconnect() = connector.disconnect()

    val standardOutput
        get() = sout.toString()

    val standardError
        get() = serr.toString()

    override fun run(): ExecutionResult {

        var failure: Throwable? = null
        try {
            buildListener?.buildStarted()
            launcher.run()
        } catch (e: BuildException) {
            failure = e.cause
        } catch (e: Exception) {
            failure = e
        } finally {
            buildListener?.buildFinished()
        }

        val stdout = standardOutput
        val tasks = buildList<MinimalExecutedTask> {
            for (taskName in tasksExecuted) {
                // Scan stdout for task's up to date
                val upToDate = taskName isUpToDate stdout
                val skipped = taskName isSkipped stdout
                add(MinimalExecutedTask(taskName, upToDate, skipped))
            }
        }
        val success = failure == null
        return ToolingExecutionResult(success, stdout, standardError, tasks, failure)
    }

    private infix fun String.isUpToDate(stdout: String) = containsOutput(stdout, "UP-TO-DATE")

    private infix fun String.isSkipped(stdout: String) = containsOutput(stdout, "SKIPPED")

    private fun String.containsOutput(stdout: String, stateIdentifier: String) = "$this $stateIdentifier" in stdout
}
