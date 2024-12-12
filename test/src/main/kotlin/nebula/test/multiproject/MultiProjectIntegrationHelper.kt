package nebula.test.multiproject

import nebula.test.Integration
import nebula.test.div
import nebula.test.plusAssign
import nebula.test.text
import java.io.File

class MultiProjectIntegrationHelper(val projectDir: File,
                                    val settingsFile: File) {

    constructor(spec: Integration) : this(spec.projectDir, spec.settingsFile!!)

    fun create(vararg projectNames: String): Map<String, MultiProjectIntegrationInfo> = create(projectNames.asList())

    infix fun create(projectNames: Collection<String>): Map<String, MultiProjectIntegrationInfo> = buildMap {

        for (name in projectNames) {
            settingsFile += "include '$name'$lineEnd"
            val dir = projectDir / name
            dir.mkdirs()
            val buildFile = dir / "build.gradle"

            put(name, MultiProjectIntegrationInfo(name, dir, buildFile))
        }
    }

    infix fun addSubproject(name: String): File {
        settingsFile += "\ninclude '$name'$lineEnd"
        val dir = projectDir / name
        dir.mkdirs()

        return dir
    }

    fun addSubproject(name: String, gradleContents: String): File =
        addSubproject(name).apply { resolve("build.gradle").text = gradleContents }

    companion object {
        val lineEnd = System.lineSeparator()
    }
}
