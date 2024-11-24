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

data class VersionDiff(val oldVersion: String, val updatedVersion: String) : Comparable<VersionDiff> {

    override fun compareTo(other: VersionDiff): Int {
        val old = oldVersion.compareTo(other.oldVersion)
        if (old != 0) return old
        return updatedVersion.compareTo(other.updatedVersion)
    }
}