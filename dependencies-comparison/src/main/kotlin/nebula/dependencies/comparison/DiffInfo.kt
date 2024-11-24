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

class DiffInfo(val versionDiff: VersionDiff): Comparable<DiffInfo> {

    val configurations = mutableSetOf<String>()

    infix fun addConfiguration(configuration: String) {
        configurations += configuration
    }

    val oldVersion: String
        get() = versionDiff.oldVersion
    val updatedVersion: String
        get() = versionDiff.updatedVersion

    override fun compareTo(other: DiffInfo): Int = versionDiff.compareTo(other.versionDiff)
}
