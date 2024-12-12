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
import nebula.test.functional.GradleRunner
import nebula.test.functional.PreExecutionAction
import java.io.File

class DefaultGradleRunner(private val handleFactory: GradleHandleFactory) : GradleRunner {

    override fun run(projectDir: File, args: List<String>) = run(projectDir, args, emptyList())
    override fun run(projectDir: File, args: List<String>, jvmArgs: List<String>) = run(projectDir, args, jvmArgs, emptyList())

    override fun run(projectDir: File,
                     args: List<String>,
                     jvmArgs: List<String>,
                     preExecutionActions: List<PreExecutionAction>): ExecutionResult {
        return handle(projectDir, args, jvmArgs, preExecutionActions).run();
    }

    override fun handle(projectDir: File, args: List<String>) = handle(projectDir, args, emptyList())
    override fun handle(projectDir: File, args: List<String>, jvmArgs: List<String>) = handle(projectDir, args, jvmArgs, emptyList())
    override fun handle(projectDir: File,
                        args: List<String>,
                        jvmArgs: List<String>,
                        preExecutionActions: List<PreExecutionAction>): GradleHandle {
        for(action in preExecutionActions)
            action(projectDir, args, jvmArgs)
        return handleFactory.start(projectDir, args, jvmArgs);
    }
}
