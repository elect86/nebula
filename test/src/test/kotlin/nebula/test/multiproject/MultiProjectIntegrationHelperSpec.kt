package nebula.test.multiproject

import nebula.test.Integration
import nebula.test.div
import nebula.test.plusAssign
import nebula.test.text
import kotlin.test.Test

class MultiProjectIntegrationHelperSpec : Integration() {

    @Test
    fun `create multi-project`() {

        helper.create("sub")

        assert(projectDir.resolve("sub").exists())
        assert(settingsFile!!.text.contains("include 'sub'"))
    }

    @Test
    fun `created multi-project can run build`() {
        helper.create("sub")

        buildFile!! += """
            |subprojects {
            |    apply plugin: 'java'
            |}""".trimMargin()

        val result = runTasksSuccessfully("build")

        //        then:
        //        noExceptionThrown()
        assert(":sub:build" in result.standardOutput)
        assert("BUILD SUCCESSFUL" in result.standardOutput)
    }

    @Test
    fun `can create multi-projects with deeper directory structure`() {

        helper.create("structure/sub")

        val structure = projectDir / "structure"
        assert(structure.isDirectory)
        assert(structure.resolve("sub").isDirectory)
        assert("include 'structure/sub'" in settingsFile!!.text)
    }

    @Test
    fun `add a subproject and build․gradle`() {
        val subBuildGradle = "   apply plugin: 'java'"

        val directory = helper.addSubproject("sub", subBuildGradle)

        assert(directory.resolve("build.gradle").text == subBuildGradle)
    }
}
