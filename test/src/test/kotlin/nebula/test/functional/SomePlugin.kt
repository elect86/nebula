package nebula.test.functional

import nebula.test.functional.foo.Thing
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hamcrest.Description

class SomePlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.task("echo") {
            it.outputs.upToDateWhen { project.properties["upToDate"]?.toString()?.toBoolean() ?: false }

            it.doLast {
                Thing() // Class in another package
                object : org.hamcrest.BaseMatcher<Any>() { // is a compile dependency, test it's available
                    override fun describeTo(p0: Description) {; TODO("Not yet implemented"); }
                    override fun matches(p0: Any): Boolean {; TODO("Not yet implemented"); }
                    override fun describeMismatch(p0: Any, p1: Description) {; TODO("Not yet implemented"); }
                }
                project.logger.quiet("I ran!")
            }
        }

        project.task("doIt") {
            it.onlyIf { project.properties["skip"]?.toString()?.toBoolean()?.not() ?: true }
            it.doLast { project.logger.quiet("Did it!") }
        }

        project.task("print").doLast {
            println("Printed (stdout)")
            System.err.println("Printed (stderr)")
        }
    }
}
