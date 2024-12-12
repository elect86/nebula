package nebula.test

import java.io.File
import kotlin.test.Test

class SystemPropertyCleanupProjectSpec : ProjectSpec() {

    lateinit var refProjectDir: File

    fun setup() {
        refProjectDir = projectDir
    }

    fun cleanupSpec() {
        assert(!refProjectDir.exists())
    }

    @Test
    fun `Cleans project directory after test`() {

        System.setProperty("CLEAN_PROJECT_DIR_SYS_PROP", true.toString())


        assert(project != null)
        assert(projectDir.exists())

        System.clearProperty("CLEAN_PROJECT_DIR_SYS_PROP")
    }
}