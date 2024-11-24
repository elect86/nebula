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

class DependencyDiff(val dependency: String): Comparable<DependencyDiff> {

    val diff = mutableMapOf<VersionDiff, DiffInfo>() //withDefault { versionDiff -> new DiffInfo (versionDiff) }
    operator fun MutableMap<VersionDiff, DiffInfo>.invoke(versionDiff: VersionDiff): DiffInfo = getOrPut(versionDiff) { DiffInfo(versionDiff) }

    fun addDiff(oldVersion: String, updatedVersion: String, configuration: String) {
        diff(VersionDiff(oldVersion, updatedVersion)).addConfiguration(configuration)
    }

    val isNew: Boolean
        get() = diff.size == 1 && diff.values.first().oldVersion == "" && diff.values.first().updatedVersion != ""

    val newDiffString: String
        get() = "  $dependency: ${diff.values.first().updatedVersion}"

    val isRemoved: Boolean
        get() = diff.size == 1 && diff.values.first().oldVersion != "" && diff.values.first().updatedVersion == ""

    val removedDiffString: String
        get() = "  $dependency"

    val isUpdated: Boolean
        get() = diff.size == 1 && diff.values.first().oldVersion != "" && diff.values.first().updatedVersion != ""

    val updatedDiffString: String
        get() = "  $dependency: ${diff.values.first().oldVersion} -> ${diff.values.first().updatedVersion}"

    val isInconsistent: Boolean
        get() = diff.size > 1

    val inconsistentDiffList: List<String>
        get() = diff.values.sorted().fold(listOf("  $dependency:")) { list, diffInfo ->
            list + "    ${diffInfo.oldVersion} -> ${diffInfo.updatedVersion} [${diffInfo.configurations.sorted().joinToString(",")}]"
        }

    override fun compareTo(other: DependencyDiff): Int = dependency.compareTo(other.dependency)
}