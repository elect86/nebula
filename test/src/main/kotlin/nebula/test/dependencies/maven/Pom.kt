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

import java.util.*


class Pom(group: String,
          artifact: String,
          version: String,
          type: Artifact.Type = Artifact.Type.jar) {

    val artifact = Artifact(group, artifact, version).also { it.type = type }
    val dependencies = TreeSet<Artifact>()
    val dependencyManagementArtifacts = TreeSet<Artifact>()

    infix fun addDependency(artifact: Artifact): Boolean = dependencies.add(artifact)

    infix fun addDependency(gav: String): Boolean {
        val (g, a, v) = gav.split(':')
        return addDependency(g, a, v)
    }

    fun addDependency(group: String, name: String, version: String): Boolean = addDependency(Artifact(group, name, version))

    infix fun addManagementDependency(artifact: Artifact): Boolean = dependencyManagementArtifacts.add(artifact)

    infix fun addManagementDependency(gav: String): Boolean {
        val (g, a, v) = gav.split(':')
        return addManagementDependency(g, a, v)
    }

    fun addManagementDependency(group: String, name: String, version: String): Boolean = addManagementDependency(Artifact(group, name, version))

    val filename: String
        get() = "${artifact.artifact}-${artifact.version}.pom"

    fun generate(): String = buildString {
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

        +"""?xml version="1.0" encoding="UTF-8"?"""
        "project"("xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\"",
                  "xmlns=\"http://maven.apache.org/POM/4.0.0\"",
                  "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"") {

            "modelVersion"("4.0.0")
            "groupId"(artifact.group)
            "artifactId"(artifact.artifact)
            "version"(artifact.version)
            if (artifact.type != Artifact.Type.jar)
                "packaging"(artifact.type)
            if (dependencyManagementArtifacts.isNotEmpty())
                "dependencyManagement" {
                    "dependencies" {
                        for (a in dependencyManagementArtifacts)
                            "dependency" {
                                "groupId"(a.group)
                                "artifactId"(a.artifact)
                                "version"(a.version)
                            }
                    }
                }
            if (dependencies.isNotEmpty())
                "dependencies" {
                    for (a in dependencies)
                        "dependency" {
                            "groupId"(a.group)
                            "artifactId"(a.artifact)
                            "version"(a.version)
                        }
                }
        }
        // remove last `\n`
    }.dropLast(1)
}

fun Pom(gav: String, type: Artifact.Type = Artifact.Type.jar): Pom {
    val parts = gav.split(':')
    return Pom(parts[0], parts[1], parts[2], type)
}