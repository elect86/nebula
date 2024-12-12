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
package nebula.test.dependencies.repositories

import nebula.test.dependencies.maven.Pom
import nebula.test.div
import nebula.test.text
import java.io.File

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

class MavenRepo {
    val poms = HashSet<Pom>()
    lateinit var root: File

    val repoString: String
        get() = """
            maven { url '${root.absolutePath}' }
            """.trimIndent()

    fun generate() {
        if (!root.exists())
            root.mkdirs()
        for (pom in poms) {
            val path = "${pom.groupAndArtifactPath}/${pom.artifact.version}"
            val dir = File(root, path).apply { mkdirs() }
            File(dir, pom.filename).text = pom.generate()
        }
        generateMavenMetadata(poms)
    }

    private fun generateMavenMetadata(poms: Set<Pom>) {
        val groupedPoms = poms.groupBy { it.groupAndArtifactPath }
        for ((groupAndArtifactPath, pomGroup) in groupedPoms) {
            val sortedPoms = pomGroup.sortedBy { it.artifact.version }
            val metadataFile = root / "$groupAndArtifactPath/maven-metadata.xml"
            metadataFile.writeText(buildString {
                var indentation = ""
                operator fun String.unaryPlus() = appendLine("$indentation<$this>")
                operator fun String.invoke(vararg args: String, block: () -> Unit) {
                    val argsOut = if (args.isEmpty()) "" else args.joinToString(" ", prefix = " ")
                    +"""$this$argsOut"""
                    indentation += "  "
                    block()
                    indentation = indentation.drop(2)
                    +"""/$this"""
                }

                operator fun String.invoke(arg: Any) = +"""$this>$arg</$this"""

                "metadata" {
                    "groupId"(sortedPoms.first().artifact.group)
                    "artifactId"(sortedPoms.first().artifact.artifact)
                    "versioning" {
                        "latest"(sortedPoms.last().artifact.version)
                        "release"(sortedPoms.last().artifact.version)
                        "versions" {
                            for (pom in sortedPoms)
                                "version"(pom.artifact.version)
                        }
                    }
                    "lastUpdated"(SimpleDateFormat("yyyyMMddHHmmss").format(Date()))
                }
            })
        }
    }

    private val Pom.groupAndArtifactPath
        get() = "${artifact.group.replace(Regex("\\."), "/")}/${artifact.artifact}"
}
