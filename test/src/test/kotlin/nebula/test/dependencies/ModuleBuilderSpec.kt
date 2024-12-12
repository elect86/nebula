package nebula.test.dependencies

import kotlin.test.Test

class ModuleBuilderSpec {

    @Test
    fun `build module with no dependencies`() {
        ModuleBuilder("test.modulebuilder:foo:1.0.0").build().apply {
            assert(group == "test.modulebuilder")
            assert(artifact == "foo")
            assert(version == "1.0.0")
            assert(dependencies.isEmpty())
        }
    }

    @Test
    fun `build module with no dependencies separate group, artifact, version`() {
        ModuleBuilder("test.modulebuilder", "foo", "1.0.0").build().apply {
            assert(group == "test.modulebuilder")
            assert(artifact == "foo")
            assert(version == "1.0.0")
            assert(dependencies.isEmpty())
        }
    }

    @Test
    fun `build module with specific status`() {
        ModuleBuilder("test.modulebuilder", "foo", "1.0.0").setStatus("snapshot").build().apply {
            assert(group == "test.modulebuilder")
            assert(artifact == "foo")
            assert(version == "1.0.0")
            assert(status == "snapshot")
        }
    }

    @Test
    fun `add dependency`() {
        ModuleBuilder("test.modulebuilder", "bar", "1.0.0")
                .addDependency("test.dependency", "baz", "2.0.1")
                .build().apply {
                    assert(dependencies.size == 1)
                    dependencies.first().apply {
                        assert(group == "test.dependency")
                        assert(artifact == "baz")
                        assert(version == "2.0.1")
                    }
                }
    }

    @Test
    fun `add dependencies`() {
        ModuleBuilder("test.modulebuilder", "bar", "1.0.0")
                .addDependencies("test.dependency:baz:2.0.1",
                                 "test.dependency:qux:42.13.0")
                .build().apply {
                    assert(dependencies.size == 2)
                    dependencies.first { it.artifact == "baz" }.apply {
                        assert(group == "test.dependency")
                        assert(artifact == "baz")
                        assert(version == "2.0.1")
                    }
                    dependencies.first { it.artifact == "qux" }.apply {
                        assert(group == "test.dependency")
                        assert(artifact == "qux")
                        assert(version == "42.13.0")
                    }
                }
    }
}
