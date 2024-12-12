package nebula.test.dependencies

import kotlin.test.Test
import kotlin.test.assertNotNull

class DependencyGraphBuilderSpec {

    @Test
    fun `add one dependency`() {
        val builder = DependencyGraphBuilder()
        builder += "test.nebula:foo:1.0.0"

        val graph = builder.build()

        assert(graph.nodes.size == 1)
        graph.nodes.first().coordinate.apply {
            assert(group == "test.nebula")
            assert(artifact == "foo")
            assert(version == "1.0.0")
        }
    }

    @Test
    fun `add one dependency with group, artifact, version syntax`() {
        val builder = DependencyGraphBuilder()
        builder.addModule("test.nebula", "foo", "1.0.0")

        val graph = builder.build()

        assert(graph.nodes.size == 1)
        graph.nodes.first().coordinate.apply {
            assert(group == "test.nebula")
            assert(artifact == "foo")
            assert(version == "1.0.0")
        }
    }

    @Test
    fun `add multiple dependencies`() {
        val builder = DependencyGraphBuilder()
        builder.addModules("test.nebula:foo:1.0.0",
                           "a.nebula:bar:2.0.0")

        val graph = builder.build()

        assert(graph.nodes.size == 2)
        graph.nodes.first { it.coordinate.artifact == "foo" }.coordinate.apply {
            assert(group == "test.nebula")
            assert(artifact == "foo")
            assert(version == "1.0.0")
        }
        graph.nodes.first { it.coordinate.artifact == "bar" }.coordinate.apply {
            assert(group == "a.nebula")
            assert(artifact == "bar")
            assert(version == "2.0.0")
        }
    }

    @Test
    fun `add module with dependencies`() {
        val builder = DependencyGraphBuilder()
        builder.addModule(ModuleBuilder("test.nebula:foo:1.0.0")
                                  .addDependency("test.nebula:bar:1.1.1")
                                  .build())

        val graph = builder.build()

        assert(graph.nodes.size == 2)
        assertNotNull(graph.nodes.find { it.artifact == "bar" })
    }

    @Test
    fun `add module with dependencies, add another module make sure it replaces with the one with dependencies`() {
        val builder = DependencyGraphBuilder()
        builder.addModules(ModuleBuilder("test.nebula:foo:1.0.0").addDependency("test.nebula:bar:1.1.1").build(),
                           ModuleBuilder("test.nebula:bar:1.1.1").addDependency("test.nebula:baz:23.1.3").build())

        val graph = builder.build()

        assert(graph.nodes.size == 3)
        graph.nodes.first { it.artifact == "bar" }.apply {
            assert(dependencies.size == 1)
            dependencies.first().apply {
                assert(group == "test.nebula")
                assert(artifact == "baz")
                assert(version == "23.1.3")
            }
        }
    }

    @Test
    fun `add module with dependencies, verify modules are not replaced with placeholder`() {
        val builder = DependencyGraphBuilder()
        builder.addModules(ModuleBuilder("test.nebula:bar:1.1.1").addDependency("test.nebula:baz:23.1.3").build(),
                           ModuleBuilder("test.nebula:foo:1.0.0").addDependency("test.nebula:bar:1.1.1").build())

        val graph = builder.build()

        assert(graph.nodes.size == 3)
        graph.nodes.first { it.artifact == "bar" }.apply {
            assert(dependencies.size == 1)
            dependencies.first().apply {
                assert(group == "test.nebula")
                assert(artifact == "baz")
                assert(version == "23.1.3")
            }
        }
    }
}
