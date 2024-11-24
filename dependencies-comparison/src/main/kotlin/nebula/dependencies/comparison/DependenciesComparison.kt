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

object DependenciesComparison {

    fun performDiff(old: ConfigurationsSet, updated: ConfigurationsSet): List<DependencyDiff> {
        val memory = mutableMapOf<String, DependencyDiff>()
        operator fun MutableMap<String, DependencyDiff>.invoke(dependency: String) = getOrPut(dependency) { DependencyDiff(dependency) }
        for (configuration in old.configurations + updated.configurations) {
            val oldDependencies = old dependenciesForConfiguration configuration
            val updatedDependencies = updated dependenciesForConfiguration configuration

            for (dependency in oldDependencies.allModules + updatedDependencies.allModules) {
                val oldVersion = oldDependencies usedVersion dependency
                val updatedVersion = updatedDependencies usedVersion dependency

                if (oldVersion != updatedVersion) {
                    val diff = memory(dependency)
                    diff.addDiff(oldVersion, updatedVersion, configuration)
                }
            }
        }

        return memory.values.sorted()
    }

    fun performDiffByConfiguration(old: ConfigurationsSet, updated: ConfigurationsSet): Map<String, List<DependencyDiff>> {
        val configurations = old.configurations + updated.configurations
        return configurations.associateWith { configuration ->
            val oldDependencies = old dependenciesForConfiguration configuration
            val updatedDependencies = updated dependenciesForConfiguration configuration

            val allDependencies = oldDependencies.allModules + updatedDependencies.allModules
            allDependencies.mapNotNull { dependency ->
                val oldVersion = oldDependencies usedVersion dependency
                val updatedVersion = updatedDependencies usedVersion dependency

                if (oldVersion != updatedVersion)
                    DependencyDiff(dependency).apply { addDiff(oldVersion, updatedVersion, configuration) }
                else
                    null
            }
        }
    }
}