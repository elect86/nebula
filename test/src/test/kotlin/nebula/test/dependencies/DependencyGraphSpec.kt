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

import kotlin.test.Test


class DependencyGraphSpec {

    @Test
    fun `one node graph`() {

        val graph = DependencyGraph("test:foo:1.0.0")

        assert(graph.nodes.size == 1)
        graph.nodes[0].apply {
            assert(group == "test")
            assert(artifact == "foo")
            assert(version == "1.0.0")
            assert(dependencies.isEmpty())
            assert(toString() == "test:foo:1.0.0:integration")
        }
    }

    @Test
    fun `node with dependencies`() {

        val graph = DependencyGraph("test:foo:1.0.0 -> test:bar:1.+")

        assert(graph.nodes.size == 1)
        graph.nodes[0].apply {
            assert(group == "test")
            assert(artifact == "foo")
            assert(version == "1.0.0")
            assert(dependencies.size == 1)
            dependencies[0].apply {
                assert(group == "test")
                assert(artifact == "bar")
                assert(version == "1.+")
            }
        }
    }

    @Test
    fun `node with multiple dependencies`() {

        val graph = DependencyGraph("test:foo:1.0.0 -> test:bar:1.+|g:a:[1.0.0,2.0.0)|g1:a1:1.1.1")

        assert(graph.nodes.size == 1)
        assert(graph.nodes[0].dependencies.size == 3)
        val dependencies = graph.nodes[0].dependencies.map { it.toString() }
        assert("test:bar:1.+" in dependencies)
        assert("g:a:[1.0.0,2.0.0)" in dependencies)
        assert("g1:a1:1.1.1" in dependencies)
    }

    @Test
    fun `check var arg constructor`() {

        val graph = DependencyGraph("test:foo:1.0.0", "test:bar:1.1.1")

        assert(graph.nodes.size == 2)
    }
}
