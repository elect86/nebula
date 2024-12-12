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

package nebula.test.functional.internal

import nebula.test.functional.ExecutionResult
import org.gradle.api.GradleException

abstract class DefaultExecutionResult(override var success: Boolean,
                                      override val standardOutput: String,
                                      override val standardError: String,
                                      val executedTasks: List<ExecutedTask>,
                                      override val failure: Throwable?) : ExecutionResult {

    override fun wasExecuted(taskPath: String): Boolean = executedTasks.any { it.path == normalizeTaskPath(taskPath) }

    override fun wasUpToDate(taskPath: String): Boolean = getExecutedTaskByPath(taskPath).upToDate

    override fun wasSkipped(taskPath: String): Boolean = getExecutedTaskByPath(taskPath).skipped

    private fun normalizeTaskPath(taskPath: String) = if (taskPath.startsWith(':')) taskPath else ":$taskPath"

    private fun getExecutedTaskByPath(taskPath: String): ExecutedTask {
        val path = normalizeTaskPath(taskPath)
        return executedTasks.find { it.path == path } ?: throw RuntimeException ("Task with path $path was not found")
    }

    override fun rethrowFailure(): ExecutionResult {
        if (failure is GradleException)
            throw failure as GradleException
        if (failure != null)
            throw GradleException ("Build aborted because of an internal error.", failure)
        return this
    }
}
