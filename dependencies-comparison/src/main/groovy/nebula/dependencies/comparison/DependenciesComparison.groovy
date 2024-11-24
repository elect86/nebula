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

class DependenciesComparison {

    List<DependencyDiff> performDiff(ConfigurationsSet old, ConfigurationsSet updated) {
        def memory = [:].withDefault { String dependency -> new DependencyDiff(dependency) }
        Set<String> configurations = old.configurations() + updated.configurations()
        configurations.forEach { configuration ->
            Dependencies oldDependencies = old.dependenciesForConfiguration(configuration)
            Dependencies updatedDependencies = updated.dependenciesForConfiguration(configuration)

            Set<String> allDependencies = oldDependencies.allModules() + updatedDependencies.allModules()
            allDependencies.forEach { dependency ->
                String oldVersion = oldDependencies.usedVersion(dependency)
                String updatedVersion = updatedDependencies.usedVersion(dependency)

                if (oldVersion != updatedVersion) {
                    DependencyDiff diff = memory.get(dependency)
                    diff.addDiff(oldVersion, updatedVersion, configuration)
                }
            }
        }

        memory.values().toSorted()
    }

    Map<String, List<DependencyDiff>> performDiffByConfiguration(ConfigurationsSet old, ConfigurationsSet updated) {
        Map
        Set<String> configurations = old.configurations() + updated.configurations()
        configurations.collectEntries { configuration ->
            Dependencies oldDependencies = old.dependenciesForConfiguration(configuration)
            Dependencies updatedDependencies = updated.dependenciesForConfiguration(configuration)

            Set<String> allDependencies = oldDependencies.allModules() + updatedDependencies.allModules()
            List<DependencyDiff> allDiffsInConfig = allDependencies.collect { dependency ->
                String oldVersion = oldDependencies.usedVersion(dependency)
                String updatedVersion = updatedDependencies.usedVersion(dependency)

                if (oldVersion != updatedVersion) {
                    DependencyDiff diff = new DependencyDiff(dependency)
                    diff.addDiff(oldVersion, updatedVersion, configuration)
                    return diff
                } else
                    return null
            }.findAll {it != null }
            [configuration, allDiffsInConfig]
        }
    }
}
