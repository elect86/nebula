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

import groovy.transform.Canonical
import groovy.transform.Sortable

@Canonical
@Sortable(includes = ['dependency'])
class DependencyDiff {
    String dependency
    Map<VersionDiff, DiffInfo> diff = [:].withDefault { versionDiff -> new DiffInfo(versionDiff) }

    void addDiff(String oldVersion, String updatedVersion, String configuration) {
        diff.get(new VersionDiff(oldVersion, updatedVersion)).addConfiguration(configuration)
    }

    Boolean isNew() {
        diff.size() == 1 && diff.values()[0].oldVersion == '' && diff.values()[0].updatedVersion != ''
    }

    String newDiffString() {
        "  $dependency: ${diff.values()[0].updatedVersion}"
    }

    Boolean isRemoved() {
        diff.size() == 1 && diff.values()[0].oldVersion != '' && diff.values()[0].updatedVersion == ''
    }

    String removedDiffString() {
        "  $dependency"
    }

    Boolean isUpdated() {
        diff.size() == 1 && diff.values()[0].oldVersion != '' && diff.values()[0].updatedVersion != ''
    }

    String updatedDiffString() {
        "  $dependency: ${diff.values()[0].oldVersion} -> ${diff.values()[0].updatedVersion}"
    }

    Boolean isInconsistent() {
        diff.size() > 1
    }

    List<String> inconsistentDiffList() {
        diff.values().sort().inject(["  $dependency:"]) { list, diffInfo ->
            list << "    ${diffInfo.oldVersion} -> ${diffInfo.updatedVersion} [${diffInfo.configurations.sort().join(',')}]"
        }
    }
}