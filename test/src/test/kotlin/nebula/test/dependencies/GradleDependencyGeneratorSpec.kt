/*
 * Copyright 2014-2017 Netflix, Inc.
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

import groovy.lang.Closure
import nebula.test.div
import nebula.test.text
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.StartParameter
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.initialization.IncludedBuild
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.invocation.GradleLifecycle
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.ObjectConfigurationAction
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.plugins.PluginManager
import org.gradle.api.services.BuildServiceRegistry
import java.io.File
import kotlin.test.Test

class GradleDependencyGeneratorSpec {

    @Test
    fun `generate a maven repo`() {
        val directory = "build/testdependencies/testmavenrepo"
        val graph = listOf("test.maven:foo:1.0.0")
        val generator = GradleDependencyGenerator(DependencyGraph(graph), directory)

        generator.generateTestMavenRepo()

        val mavenRepo = File("$directory/mavenrepo")
        assert(File(mavenRepo, "test/maven/foo/1.0.0/foo-1.0.0.pom").exists())
        assert(File(mavenRepo, "test/maven/foo/1.0.0/foo-1.0.0.jar").exists())
    }

    @Test
    fun `generate a maven repo with a SNAPSHOT`() {
        val directory = "build/testdependencies/testmavenreposnapshot"
        val graph = listOf("test.maven:foo:1.0.1-SNAPSHOT")
        val generator = GradleDependencyGenerator(DependencyGraph(graph), directory)

        generator.generateTestMavenRepo()

        val mavenRepo = File("$directory/mavenrepo")
        assert(File(mavenRepo, "test/maven/foo/1.0.1-SNAPSHOT/").listFiles()!!.any {
            Regex("foo-1\\.0\\.1-\\d{8}\\.\\d{6}-\\d?\\.pom").containsMatchIn(it.name)
        })
    }

    @Test
    // using one dot leader as a dummy point in fun name, https://www.compart.com/en/unicode/U+2024
    fun `publishes ivy status using DependencyGraphBuilder․addModule - passing status`() {
        val directory = "build/testdependencies/ivypublishAddModuleWithStatus"
        val graph = DependencyGraphBuilder().addModules("test.resolved:a:1.0.0:release",
                                                        "test.resolved:a:1.1.0:release").build()
        val generator = GradleDependencyGenerator(graph, directory)

        generator.generateTestIvyRepo()

        val ivyRepo = File("build/testdependencies/ivypublishAddModuleWithStatus")
        val xml = ivyRepo / "ivyrepo/test/resolved/a/1.1.0/a-1.1.0-ivy.xml"
        assert(xml.exists())
        assert("status=\"release\"" in xml.text)
    }

    @Test
    fun `publishes ivy status using DependencyGraphBuilder․addModule`() {
        val directory = "build/testdependencies/ivypublishAddModule"
        val graph = DependencyGraphBuilder().addModules("test.resolved:a:1.0.0",
                                                        "test.resolved:a:1.1.0").build()
        val generator = GradleDependencyGenerator(graph, directory)

        generator.generateTestIvyRepo()

        val ivyRepo = File("build/testdependencies/ivypublishAddModule")
        val xml = ivyRepo / "ivyrepo/test/resolved/a/1.1.0/a-1.1.0-ivy.xml"
        assert(xml.exists())
        assert("status=\"integration\"" in xml.text)
    }

    @Test
    fun `DependencyGraphBuilder using map for DependencyGraph`() {
        val directory = "build/testdependencies/ivypublishMap"
        val graph = listOf("test.resolved:a:1.0.0", "test.resolved:a:1.1.0")
        val generator = GradleDependencyGenerator(DependencyGraph(graph), directory)

        generator.generateTestIvyRepo()

        val ivyRepo = File("build/testdependencies/ivypublishMap")
        val xml = ivyRepo / "ivyrepo/test/resolved/a/1.1.0/a-1.1.0-ivy.xml"
        assert(xml.exists())
        assert("status=\"integration\"" in xml.text)
    }

    @Test
    fun `generate an ivy repo`() {
        val directory = "build/testdependencies/testivyrepo"
        val graph = listOf("test.ivy:foo:1.0.0")
        val generator = GradleDependencyGenerator(DependencyGraph(graph), directory)

        generator.generateTestIvyRepo()

        val ivyRepo = File("$directory/ivyrepo")
        assert(File(ivyRepo, "test/ivy/foo/1.0.0/foo-1.0.0-ivy.xml").exists())
        assert(File(ivyRepo, "test/ivy/foo/1.0.0/foo-1.0.0.jar").exists())
    }

    @Test
    fun `generate an ivy repo block - gradle version older than 5․x`() {
        val directory = "build/testdependencies/testivyrepo"
        val graph = listOf("test.ivy:foo:1.0.0")
        val generator = GradleDependencyGenerator("4.10.3", DependencyGraph(graph), directory)

        val block = generator.ivyRepositoryBlock

        assert("""
            |   layout('pattern') {
            |        ivy '[organisation]/[module]/[revision]/[module]-[revision]-ivy.[ext]'
            |        artifact '[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]'
            |        m2compatible = true
            |    }
        """.trimMargin() in block)
    }

    @Test
    fun `generate an ivy repo block - uses Gradle object newer versions of Gradle`() {
        val gradleMock = gradleMock("5.2.1")
        val directory = "build/testdependencies/testivyrepo"
        val graph = listOf("test.ivy:foo:1.0.0")

        val generator = GradleDependencyGenerator(gradleMock, DependencyGraph(graph), directory)
        val block = generator.ivyRepositoryBlock

        assert("""
            |    patternLayout {
            |        ivy '[organisation]/[module]/[revision]/[module]-[revision]-ivy.[ext]'
            |        artifact '[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]'
            |        m2compatible = true
            |    }""".trimMargin() in block)
    }

    @Test
    fun `generate an ivy repo block - uses Gradle object old versions of Gradle`() {
        val gradleMock = gradleMock("4.10.3")
        val directory = "build/testdependencies/testivyrepo"
        val graph = listOf("test.ivy:foo:1.0.0")

        val generator = GradleDependencyGenerator(gradleMock, DependencyGraph(graph), directory)
        val block = generator.ivyRepositoryBlock

        assert("""
            |    layout('pattern') {
            |        ivy '[organisation]/[module]/[revision]/[module]-[revision]-ivy.[ext]'
            |        artifact '[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]'
            |        m2compatible = true
            |    }""".trimMargin() in block)
    }

    @Test
    fun `generate an ivy repo block - gradle version newer than 5․x`() {
        val directory = "build/testdependencies/testivyrepo"
        val graph = listOf("test.ivy:foo:1.0.0")
        val generator = GradleDependencyGenerator("5.2.1", DependencyGraph(graph), directory)

        val block = generator.ivyRepositoryBlock

        assert("""
            |   patternLayout {
            |        ivy '[organisation]/[module]/[revision]/[module]-[revision]-ivy.[ext]'
            |        artifact '[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]'
            |        m2compatible = true
            |    }
            """.trimMargin() in block)
    }

    @Test
    fun `check ivy status`() {
        val directory = "build/testdependencies/ivyxml"
        val graph = listOf("test.ivy:foo:1.0.0")
        val generator = GradleDependencyGenerator(DependencyGraph(graph), directory)

        generator.generateTestIvyRepo()

        val repo = File(directory)
        assert("status=\"integration\"" in File(repo, "ivyrepo/test/ivy/foo/1.0.0/foo-1.0.0-ivy.xml").text)
    }

    @Test
    fun `allow different ivy status`() {
        val directory = "build/testdependencies/ivyxml"
        val graph = listOf(DependencyGraphNode(Coordinate("test.ivy:foo-final:1.0.0"), status = "release"),
                           DependencyGraphNode(Coordinate("test.ivy:foo-candidate:1.0.0"), status = "candidate"))
        val generator = GradleDependencyGenerator(DependencyGraph("nodes" to graph), directory)

        generator.generateTestIvyRepo()

        val repo = File(directory)
        assert("status=\"release\"" in File(repo, "ivyrepo/test/ivy/foo-final/1.0.0/foo-final-1.0.0-ivy.xml").text)
        assert("status=\"candidate\"" in File(repo, "ivyrepo/test/ivy/foo-candidate/1.0.0/foo-candidate-1.0.0-ivy.xml").text)
    }

    @Test
    fun `allow different target compatibility`() {
        val directory = "build/testdependencies/testmavenrepo"
        val graph = listOf(DependencyGraphNode(Coordinate("test.maven:foo-final:1.0.0"), status = "release", targetCompatibility = JavaVersion.VERSION_1_7))
        val generator = GradleDependencyGenerator(DependencyGraph("nodes" to graph), directory)

        generator.generateTestMavenRepo()

        val repo = File(directory)
        assert("\"org.gradle.jvm.version\": 7" in File(repo, "mavenrepo/test/maven/foo-final/1.0.0/foo-final-1.0.0.module").text)
    }

    @Test
    fun `check ivy xml`() {
        val directory = "build/testdependencies/ivyxml"
        val graph = listOf("test.ivy:foo:1.0.0 -> test.ivy:bar:1.1.0")
        val generator = GradleDependencyGenerator(DependencyGraph(graph), directory)

        generator.generateTestIvyRepo()

        val repo = File(directory)
        assert("api \'test.ivy:bar:1.1.0\'" in File(repo, "test.ivy.foo_1_0_0/build.gradle").text)
        assert("<dependency org=\"test.ivy\" name=\"bar\" rev=\"1.1.0\" conf=\"compile-&gt;default\"/>" in File(repo, "ivyrepo/test/ivy/foo/1.0.0/foo-1.0.0-ivy.xml").text)
    }

    @Test
    fun `check maven pom`() {
        val directory = "build/testdependencies/mavenpom"
        val graph = listOf("test.maven:foo:1.0.0 -> test.maven:bar:1.+")
        val generator = GradleDependencyGenerator(DependencyGraph(graph), directory)

        generator.generateTestMavenRepo()

        val repo = File(directory)
        assert("api \'test.maven:bar:1.+\'" in File(repo, "test.maven.foo_1_0_0/build.gradle").text)
        val pom = File(repo, "mavenrepo/test/maven/foo/1.0.0/foo-1.0.0.pom").text
        assert("<groupId>test.maven</groupId>" in pom)
        assert("<artifactId>bar</artifactId>" in pom)
        assert("<version>1.+</version>" in pom)
        assert("<scope>compile</scope>" in pom)
    }

    @Test
    fun `multiple libraries with dependencies`() {
        val graph = listOf("integration.test:foo:1.0.0",
                           "integration.test:foo:1.0.1",
                           "integration.test:bar:1.0.0 -> integration.test:foo:1.+",
                           "integration.test:baz:0.9.0 -> integration.test:foo:[1.0.0,2.0.0)|integration.test:bar:1.0.+")

        val generator = GradleDependencyGenerator(DependencyGraph(graph))

        generator.generateTestMavenRepo()
        generator.generateTestIvyRepo()

        val mavenRepo = File("build/testrepogen/mavenrepo")
        val group = "integration.test"
        assert(mavenFilesExist(group, "foo", "1.0.0", mavenRepo))
        assert(mavenFilesExist(group, "foo", "1.0.1", mavenRepo))
        assert(mavenFilesExist(group, "bar", "1.0.0", mavenRepo))
        assert(mavenFilesExist(group, "baz", "0.9.0", mavenRepo))
        val ivyRepo = File("build/testrepogen/ivyrepo")
        assert(ivyFilesExist(group, "foo", "1.0.0", ivyRepo))
        assert(ivyFilesExist(group, "foo", "1.0.1", ivyRepo))
        assert(ivyFilesExist(group, "bar", "1.0.0", ivyRepo))
        assert(ivyFilesExist(group, "baz", "0.9.0", ivyRepo))
    }

    @Test
    fun `generator returns location of the ivy repository`() {
        val generator = GradleDependencyGenerator(DependencyGraph("test.ivy:foo:1.0.0"))

        val dir = generator.generateTestIvyRepo()

        assert(dir == File("build/testrepogen/ivyrepo"))
    }

    @Test
    fun `ask generator for location of the ivy repository`() {
        val generator = GradleDependencyGenerator(DependencyGraph("test.ivy:foo:1.0.0"), "build/test")

        val dir = generator.ivyRepoDir

        assert(dir == File("build/test/ivyrepo"))
    }

    @Test
    fun `ask generator for string location of the ivy repository`() {
        val generator = GradleDependencyGenerator(DependencyGraph("test.ivy:foo:1.0.0"), "build/test")

        val name = generator.ivyRepoDirPath

        assert(name == File("build/test/ivyrepo").absolutePath)
    }

    @Test
    fun `integration spec ivy repository block is available`() {
        val generator = GradleDependencyGenerator(DependencyGraph("test.ivy:foo:1.0.0"), "build/test")
        val expectedBlock = """
            ivy {
                url '${generator.ivyRepoUrl}'
                patternLayout {
                    ivy '[organisation]/[module]/[revision]/[module]-[revision]-ivy.[ext]'
                    artifact '[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]'
                    m2compatible = true
                }
            }
        """.trimIndent()

        val block = generator.ivyRepositoryBlock

        assert(block == expectedBlock)
    }

    @Test
    fun `generator returns location of the maven repository`() {
        val generator = GradleDependencyGenerator(DependencyGraph("test.maven:foo:1.0.0"))

        val dir = generator.generateTestMavenRepo()

        assert(dir == File("build/testrepogen/mavenrepo"))
    }

    @Test
    fun `ask generator for location of the maven repository`() {
        val generator = GradleDependencyGenerator(DependencyGraph("test.maven:foo:1.0.0"), "build/test")

        val dir = generator.mavenRepoDir

        assert(dir == File("build/test/mavenrepo"))
    }

    @Test
    fun `ask generator for string location of the maven repository`() {
        val generator = GradleDependencyGenerator(DependencyGraph("test.maven:foo:1.0.0"), "build/testmaven")

        val name = generator.mavenRepoDirPath

        assert(name == File("build/testmaven/mavenrepo").absolutePath)
    }

    @Test
    fun `integration spec maven repository block is available`() {
        val generator = GradleDependencyGenerator(DependencyGraph("test.maven:foo:1.0.0"), "build/test")
        val expectedBlock = """
            maven { url '${generator.mavenRepoUrl}' }
        """.trimIndent()

        val block = generator.mavenRepositoryBlock

        assert(block == expectedBlock)
    }

    private fun mavenFilesExist(group: String, artifact: String, version: String, repository: File): Boolean {
        val baseName = artifactPath(group, artifact, version)
        val pomExists = File(repository, "$baseName.pom").exists()
        val jarExists = File(repository, "$baseName.jar").exists()

        return pomExists && jarExists
    }

    private fun ivyFilesExist(group: String, artifact: String, version: String, repository: File): Boolean {
        val baseName = artifactPath(group, artifact, version)
        val ivyExists = File(repository, "$baseName-ivy.xml").exists()
        val jarExists = File(repository, "$baseName.jar").exists()

        return ivyExists && jarExists
    }

    fun artifactPath(group: String, artifact: String, version: String) =
        "${group.replace(Regex("\\."), "/")}/$artifact/$version/$artifact-$version"

    fun gradleMock(version: String) = object : Gradle {
        override fun getPlugins(): PluginContainer = TODO("Not yet implemented")
        override fun apply(closure: Closure<*>) = TODO("Not yet implemented")
        override fun apply(action: Action<in ObjectConfigurationAction>) = TODO("Not yet implemented")
        override fun apply(options: MutableMap<String, *>) = TODO("Not yet implemented")
        override fun getPluginManager(): PluginManager = TODO("Not yet implemented")
        override fun getExtensions(): ExtensionContainer = TODO("Not yet implemented")
        override fun getGradleVersion(): String = version
        override fun getGradleUserHomeDir(): File = TODO("Not yet implemented")
        override fun getGradleHomeDir(): File = TODO("Not yet implemented")
        override fun getParent(): Gradle = TODO("Not yet implemented")
        override fun getRootProject(): Project = TODO("Not yet implemented")
        override fun rootProject(action: Action<in Project>) = TODO("Not yet implemented")
        override fun allprojects(action: Action<in Project>) = TODO("Not yet implemented")
        override fun getTaskGraph(): TaskExecutionGraph = TODO("Not yet implemented")
        override fun getStartParameter(): StartParameter = TODO("Not yet implemented")
        override fun addProjectEvaluationListener(listener: ProjectEvaluationListener): ProjectEvaluationListener = TODO("Not yet implemented")
        override fun removeProjectEvaluationListener(listener: ProjectEvaluationListener) = TODO("Not yet implemented")
        override fun getLifecycle(): GradleLifecycle = TODO("Not yet implemented")
        override fun beforeProject(closure: Closure<*>) = TODO("Not yet implemented")
        override fun beforeProject(action: Action<in Project>) = TODO("Not yet implemented")
        override fun afterProject(closure: Closure<*>) = TODO("Not yet implemented")
        override fun afterProject(action: Action<in Project>) = TODO("Not yet implemented")
        override fun beforeSettings(closure: Closure<*>) = TODO("Not yet implemented")
        override fun beforeSettings(action: Action<in Settings>) = TODO("Not yet implemented")
        override fun settingsEvaluated(closure: Closure<*>) = TODO("Not yet implemented")
        override fun settingsEvaluated(action: Action<in Settings>) = TODO("Not yet implemented")
        override fun projectsLoaded(closure: Closure<*>) = TODO("Not yet implemented")
        override fun projectsLoaded(action: Action<in Gradle>) = TODO("Not yet implemented")
        override fun projectsEvaluated(closure: Closure<*>) = TODO("Not yet implemented")
        override fun projectsEvaluated(action: Action<in Gradle>) = TODO("Not yet implemented")
        override fun buildFinished(closure: Closure<*>) = TODO("Not yet implemented")
        override fun buildFinished(action: Action<in BuildResult>) = TODO("Not yet implemented")
        override fun addBuildListener(buildListener: BuildListener) = TODO("Not yet implemented")
        override fun addListener(listener: Any) = TODO("Not yet implemented")
        override fun removeListener(listener: Any) = TODO("Not yet implemented")
        override fun useLogger(logger: Any) = TODO("Not yet implemented")
        override fun getGradle(): Gradle = TODO("Not yet implemented")
        override fun getSharedServices(): BuildServiceRegistry = TODO("Not yet implemented")
        override fun getIncludedBuilds(): MutableCollection<IncludedBuild> = TODO("Not yet implemented")
        override fun includedBuild(name: String): IncludedBuild = TODO("Not yet implemented")
    }
}
