/*
 * Copyright 2013 the original author or authors.
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

import nebula.test.functional.internal.ExecutedTask

/**
 * @author Marcin Erdmann
 */
class MinimalExecutedTask(override val path: String,
                          override val upToDate: Boolean,
                          override val skipped: Boolean) : ExecutedTask {

    override fun toString() = "executed $path"
}
