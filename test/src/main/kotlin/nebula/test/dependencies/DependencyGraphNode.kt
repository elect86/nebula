/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.test.dependencies

import org.gradle.api.JavaVersion

class DependencyGraphNode(val coordinate: Coordinate,
                          val dependencies: List<Coordinate> = emptyList(),
                          val status: String = "integration",
                          val targetCompatibility: JavaVersion = JavaVersion.VERSION_1_8) {

    val group: String
        get() = coordinate.group
    val artifact: String
        get() = coordinate.artifact
    val version: String
        get() = coordinate.version

    override fun toString(): String = "$group:$artifact:$version:$status"
}
