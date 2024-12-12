/*
 * Copyright 2016 Netflix, Inc.
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
package nebula.test.dependencies.maven

data class Artifact(val group: String,
                    val artifact: String,
                    val version: String) : Comparable<Artifact> {

    var type = Type.jar

    val gav
        get() = "$group:$artifact:$version"

    enum class Type { pom, jar }

    override fun compareTo(other: Artifact): Int = gav.compareTo(other.gav)

    override fun toString(): String = "$group:$artifact:$version, $type"
}


fun Artifact(gav: String): Artifact {
    val parts = gav.split(':')
    return Artifact(parts[0], parts[1], parts[2])
}