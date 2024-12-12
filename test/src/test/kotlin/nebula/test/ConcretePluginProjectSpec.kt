package nebula.test

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class ConcretePluginProjectSpec : PluginProjectSpec() {

    override val pluginName = "fake-plugin"

    @Test
    fun `apply does not throw exceptions`() {

        project.apply { it.plugin(pluginName)}

        //        then:
        //        noExceptionThrown()
    }

    @Test
    fun `apply is idempotent`() {

        project.apply { it.plugin(pluginName)}
        project.apply { it.plugin(pluginName)}

//        then:
//        noExceptionThrown()
    }

    @Test
    fun `apply is fine on all levels of multiproject`() {
        val sub = project createSubproject "sub"
        project.subprojects += sub

        project.apply { it.plugin(pluginName) }
        sub.apply { it.plugin(pluginName) }

//        then:
//        noExceptionThrown()
    }

    @Test
    fun `apply to multiple subprojects`() {

        val subprojectNames = listOf("sub1", "sub2", "sub3")

        for (name in subprojectNames) {
            val subproject = project createSubproject name
            project.subprojects += subproject
        }

        project.apply { it.plugin(pluginName) }

        for (name in subprojectNames) {
            val subproject = project.subprojects.first { it.name == name }
            subproject.apply { it.plugin(pluginName) }
        }

//        then:
//        noExceptionThrown()
    }

    infix fun Project.createSubproject(name: String) =
        ProjectBuilder.builder().withName(name).withProjectDir(projectDir / name).withParent(this).build()
}
