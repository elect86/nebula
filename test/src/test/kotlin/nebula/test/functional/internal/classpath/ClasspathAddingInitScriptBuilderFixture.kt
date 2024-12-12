package nebula.test.functional.internal.classpath

import nebula.test.div
import org.gradle.util.internal.GFileUtils
import java.io.File

object ClasspathAddingInitScriptBuilderFixture {

    fun createLibraries(projectDir: File, numberOfLibs: Int = 500): List<File> =
        buildList {
            repeat(numberOfLibs) {
                val libDir = projectDir / "build/libs"
                libDir.mkdirs()
                val jar = libDir / "lib$it.jar"
                GFileUtils.touch(jar)
                add(jar)
            }
        }
}
