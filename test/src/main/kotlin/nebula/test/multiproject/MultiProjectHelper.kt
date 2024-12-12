package nebula.test.multiproject

import nebula.test.div
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class MultiProjectHelper(val parent: Project) {

    fun create(vararg projectNames: String): Map<String, MultiProjectInfo> = create(projectNames.asList())

    infix fun create(projectNames: Collection<String>): Map<String, MultiProjectInfo> = buildMap {
        for (name in projectNames) {
            val subproject = ProjectBuilder.builder().withName(name).withParent(parent).build()
            put(name, MultiProjectInfo(name, subproject, parent))
        }
    }

    fun createWithDirectories(vararg projectNames: String): Map<String, MultiProjectInfo> =
        createWithDirectories(projectNames.asList())

    infix fun createWithDirectories(projectNames: Collection<String>): Map<String, MultiProjectInfo> = buildMap {
        for (name in projectNames) {
            val subDirectory = parent.projectDir / name
            subDirectory.mkdirs()
            val subproject = ProjectBuilder.builder().withName(name).withProjectDir(subDirectory).withParent(parent).build()
            put(name, MultiProjectInfo(name, subproject, parent, subDirectory))
        }
    }

    infix fun addSubproject(name: String): Project = ProjectBuilder.builder().withName(name).withParent(parent).build()

    infix fun addSubprojectWithDirectory(name: String): Project {
        val dir = parent.projectDir / name
        dir.mkdirs()
        return ProjectBuilder.builder().withName(name).withProjectDir(dir).withParent(parent).build()
    }
}
