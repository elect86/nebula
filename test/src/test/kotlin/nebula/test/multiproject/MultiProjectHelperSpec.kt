package nebula.test.multiproject

import nebula.test.ProjectSpec
import nebula.test.div
import org.gradle.api.Project
import kotlin.test.Test

class MultiProjectHelperSpec : ProjectSpec() {

    @Test
    fun `create single subproject in multiproject`() {

        val info = helper.create("sub")

        assert(info["sub"]!!.project.parent == project)
        assert(info["sub"]!!.directory == null)
        assert(project.subprojects.find { it.name == "sub" } != null)
    }

    @Test
    fun `create single subproject with directory in multiproject`() {

        val info = helper.createWithDirectories("sub")

        assert(info["sub"]!!.directory == project.projectDir / "sub")
    }

    @Test
    fun `create multiple subproject in multiproject`() {

        val info = helper.create("sub1", "sub2")

        assert(info["sub1"]!!.project.parent == project)
        assert(info["sub1"]!!.directory == null)
        assert(project.subprojects.find { it.name == "sub1" } != null)
        assert(info["sub2"]!!.project.parent == project)
        assert(info["sub2"]!!.directory == null)
        assert(project.subprojects.find { it.name == "sub2" } != null)
    }

    @Test
    fun `add a subproject`() {

        val sub = addSubproject("sub")

        assert(sub.parent == project)
        assert(project.subprojects.find { it == sub } != null)
    }

    @Test
    fun `add a subproject with directory`() {

        val sub = addSubprojectWithDirectory("sub")

        assert(sub.parent == project)
        assert(project.subprojects.find { it == sub } != null)
        assert(sub.projectDir.toURI().toURL() == projectDir.resolve("sub").toURI().toURL())
    }
}
