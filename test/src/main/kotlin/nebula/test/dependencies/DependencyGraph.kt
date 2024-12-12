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

class DependencyGraph(graph: List<String>) {

    var nodes: List<DependencyGraphNode> = graph.map(::parseNode)

    constructor (vararg graph: String) : this(graph.toList())

    constructor(tuple: Pair<String, List<DependencyGraphNode>>): this(mapOf(tuple))

    constructor(map: Map<String, List<DependencyGraphNode>>): this() {
        nodes = map["nodes"]!!
    }

    companion object {
        private fun parseNode(s: String): DependencyGraphNode {
            // Don't use tokenize, it'll make each character a possible delimiter, e.g. \t\n would tokenize on both
            // \t OR \n, not the combination of \t\n.
            val parts = s.split("->")
            val coordinate = Coordinate(parts[0])
            val dependencies = if (parts.size > 1) parts[1].parseDependencies() else emptyList()

            return DependencyGraphNode(coordinate, dependencies, "integration")
        }

        private fun String.parseDependencies(): List<Coordinate> = split('|').map(::Coordinate)
    }
}
