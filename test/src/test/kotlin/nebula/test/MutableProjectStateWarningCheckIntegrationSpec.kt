package nebula.test

import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import java.util.function.BooleanSupplier
import kotlin.test.Test

//@IgnoreIf({ System.getenv('TITUS_TASK_ID') })
@DisabledIfEnvironmentVariable(named = "TITUS_TASK_ID", matches = "*")
class MutableProjectStateWarningCheckIntegrationSpec: Integration() {

    @Test
    fun `mutable project state warning when configuration in another project is resolved unsafely`() {

        settingsFile!! += """rootProject.name = "foo""""
        addSubproject("bar", """
            |repositories {
            |   mavenCentral()  
            |}
            |
            |configurations {
            |   bar
            |}
            |
            |dependencies {
            |   bar group: 'junit', name: 'junit', version: '4.12'
            |}""".trimMargin())
        buildFile!! += """
            |task resolve {
            |   doLast {
            |       println project(':bar').configurations.bar.files
            |   }
            |}""".trimMargin()

        val e = assertThrows<IllegalArgumentException> { runTasks("resolve", "--parallel", "--warning-mode", "all") }
        assert("Mutable Project State warnings were found (Set the ignoreMutableProjectStateWarnings system property during the test to ignore)" in e.message!!)
    }

    @Test
    fun `mutable project state warning when configuration is resolved from a non-gradle thread`() {

        settingsFile!! += "\nrootProject.name = \"foo\""
        addSubproject("bar", """
            |repositories {
            |   mavenCentral()  
            |}
            |
            |configurations {
            |   bar
            |}
            |
            |dependencies {
            |   bar group: 'junit', name: 'junit', version: '4.12'
            |}""".trimMargin())
        buildFile!! += """
            |task resolve {
            |   def thread = new Thread({
            |       println project(':bar').configurations.bar.files
            |   })
            |   doFirst {
            |       thread.start()
            |       thread.join()
            |   }
            |}""".trimMargin()

        val e = assertThrows<IllegalArgumentException> { runTasks("resolve", "--warning-mode", "all") }

        assert("Mutable Project State warnings were found (Set the ignoreMutableProjectStateWarnings system property during the test to ignore)" in e.message!!)
    }

    @Test
    fun `mutable project state warning when configuration is resolved while evaluating a different project`() {

        settingsFile!! += """
            rootProject.name = "foo"
            include ":bar", ":baz"
        """

        buildFile!! += """
            |project(':baz') {
            |   repositories {
            |       mavenCentral()
            |   }
            |
            |   configurations {
            |       baz
            |   }
            |
            |   dependencies {
            |       baz group: 'junit', name: 'junit', version: '4.12'
            |   }
            |}
            |
            |project(':bar') {
            |   println project(':baz').configurations.baz.files
            |}""".trimMargin()

        val e = assertThrows<IllegalArgumentException> { runTasks(":bar:help", "--parallel", "--warning-mode", "all") }

        assert("Mutable Project State warnings were found (Set the ignoreMutableProjectStateWarnings system property during the test to ignore)" in e.message!!)
    }
}
