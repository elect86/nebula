package nebula.test.functional.internal.classpath

import nebula.test.ProjectSpec
import nebula.test.text
import org.gradle.util.internal.TextUtil
import kotlin.test.Test

class ClasspathAddingInitScriptBuilderIntegrationTest: ProjectSpec() {

    @Test
    fun `can build init script with huge amount of dependencies`() {

        val initScript = project.file("build/init.gradle")
        val libs = ClasspathAddingInitScriptBuilderFixture.createLibraries(projectDir)

        ClasspathAddingInitScriptBuilder.build(initScript, libs)

        assert(initScript.exists ())
        val initScriptContent = initScript.text

        for(lib in libs)
            assert("classpath files('${TextUtil.escapeString(lib.absolutePath)}')" in initScriptContent)
    }
}
