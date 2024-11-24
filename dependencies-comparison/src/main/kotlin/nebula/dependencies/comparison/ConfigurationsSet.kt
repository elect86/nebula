/*
 * Copyright 2015-2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.dependencies.comparison

class ConfigurationsSet(val configurationsWithDependencies: Map<String, Dependencies>) {

    constructor(vararg configurations: Pair<String, Dependencies>) : this(mapOf(*configurations))

    infix fun dependenciesForConfiguration(configuration: String): Dependencies =
        configurationsWithDependencies.getOrDefault(configuration, Dependencies())

    val configurations: Set<String>
        get() = configurationsWithDependencies.keys

    val isEmpty: Boolean
        get() = configurationsWithDependencies.isEmpty()
}
