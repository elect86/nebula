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

import nebula.test.GradleVersion
import nebula.test.div
import nebula.test.text
import org.gradle.api.invocation.Gradle
import org.gradle.tooling.GradleConnector
import org.gradle.util.GradleVersion
import java.io.File
import java.net.URL

class GradleDependencyGenerator(gradleVersion: String,
                                val graph: DependencyGraph,
                                directory: String = "build/testrepogen") {

    val gradleVersion = GradleVersion(gradleVersion)

    private var generated = false

    val gradleRoot = File(directory)
    val ivyRepoDir = gradleRoot / "ivyrepo"
    val mavenRepoDir = gradleRoot / "mavenrepo"

    init {
        generateGradleFiles()
    }

    constructor(gradle: Gradle, graph: DependencyGraph, directory: String = "build/testrepogen") :
            this(gradle.gradleVersion, graph, directory)

    constructor(graph: DependencyGraph, directory: String = "build/testrepogen") :
            this(DEFAULT_GRADLE_VERSION, graph, directory)

    fun generateTestMavenRepo(): File {
        runTasks("publishMavenPublicationToMavenRepository")
        return mavenRepoDir
    }

    val mavenRepoDirPath: String
        get() = mavenRepoDir.absolutePath

    val mavenRepoUrl: URL
        get() = mavenRepoDir.toURI().toURL()

    val mavenRepositoryBlock: String
        get() = """
                maven { url '$mavenRepoUrl' }
            """.trimIndent()

    fun generateTestIvyRepo(): File {
        runTasks("publishIvyPublicationToIvyRepository")
        return ivyRepoDir
    }

    val ivyRepoDirPath: String
        get() = ivyRepoDir.absolutePath

    val ivyRepoUrl: URL
        get() = ivyRepoDir.toURI().toURL()

    val ivyRepositoryBlock: String
        get() {
            val isGradleOlderThanGradleFive = gradleVersion < `GRADLE_5․0․0`
            val layoutPattern = if (isGradleOlderThanGradleFive) LEGACY_PATTERN_LAYOUT else PATTERN_LAYOUT
            return """
                ivy {
                    url '$ivyRepoUrl'
                    $layoutPattern {
                        ivy '[organisation]/[module]/[revision]/[module]-[revision]-ivy.[ext]'
                        artifact '[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]'
                        m2compatible = true
                    }
                }
            """.trimIndent()
        }

    private fun generateGradleFiles() {
        if (generated)
            return
        else
            generated = true

        gradleRoot.mkdirs()
        val rootBuildGradle = gradleRoot / BUILD_GRADLE
        rootBuildGradle.text = if (gradleVersion < `GRADLE_5․0․0`) LEGACY_STANDARD_SUBPROJECT_BLOCK else STANDARD_SUBPROJECT_BLOCK
        val includes = arrayListOf<String>()
        for (n in graph.nodes) {
            val subName = "${n.group}.${n.artifact}_${n.version.replace(Regex("\\."), "_")}"
            includes += subName
            val subfolder = gradleRoot / subName
            subfolder.mkdir()
            val subBuildGradle = subfolder / BUILD_GRADLE
            subBuildGradle.text = generateSubBuildGradle(n)
        }
        val settingsGradle = gradleRoot / "settings.gradle"
        settingsGradle.text = "include " + includes.joinToString { "'$it'" }
    }

    private fun generateSubBuildGradle(node: DependencyGraphNode): String = buildString {
        append("""
            group = '${node.group}'
            version = '${node.version}'
            ext {
                artifactName = '${node.artifact}'
            }
            
            targetCompatibility = ${node.targetCompatibility}
            
            publishing {
                publications {
                    maven(MavenPublication) {
                        artifactId artifactName
                        from components.java
                    }
                    ivy(IvyPublication) {
                        module artifactName
                        from components.java
                        descriptor.status = '${node.status}'
                    }
                }
            }
        """.trimIndent())
        if (node.dependencies.isNotEmpty()) {
            appendLine("\ndependencies {")
            for (dep in node.dependencies)
                appendLine("    api '$dep'")
            appendLine('}')
        }
    }

    private fun runTasks(vararg tasks: String) {
        GradleConnector.newConnector() // Could optionally use Launcher
                .forProjectDirectory(gradleRoot)
                .connect().use {
                    it.newBuild()
                            .forTasks(*tasks)
                            .run()
                }
    }

    companion object {
        private val DEFAULT_GRADLE_VERSION = "5.2.1"
        private val `GRADLE_5․0․0` = GradleVersion("5.0.0")
        private const val LEGACY_PATTERN_LAYOUT = "layout('pattern')"
        private const val PATTERN_LAYOUT = "patternLayout"

        val STANDARD_SUBPROJECT_BLOCK = """
        subprojects {
            apply plugin: 'maven-publish'
            apply plugin: 'ivy-publish'
            apply plugin: 'java-library'

            publishing {
                repositories {
                    maven {
                        url "../mavenrepo"
                    }
                    ivy {
                        url "../ivyrepo"
                        patternLayout {
                            ivy '[organisation]/[module]/[revision]/[module]-[revision]-ivy.[ext]'
                            artifact '[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]'
                            m2compatible = true
                        }
                    }
                }
            }
        }
        """.trimIndent()

        val LEGACY_STANDARD_SUBPROJECT_BLOCK = """
        subprojects {
            apply plugin: 'maven-publish'
            apply plugin: 'ivy-publish'
            apply plugin: 'java-library'

            publishing {
                repositories {
                    maven {
                        url "../mavenrepo"
                    }
                    ivy {
                        url "../ivyrepo"
                        layout('pattern') {
                            ivy '[organisation]/[module]/[revision]/[module]-[revision]-ivy.[ext]'
                            artifact '[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]'
                            m2compatible = true
                        }
                    }
                }
            }
        }
        """.trimIndent()

        const val BUILD_GRADLE = "build.gradle"
    }
}
