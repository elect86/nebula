package nebula.test

import java.io.File
import kotlin.test.Test

class CustomCleanupProjectSpec : ProjectSpec() {

    //    @Shared
    lateinit var refProjectDir: File

    fun setup() {
        refProjectDir = projectDir
    }

    fun cleanupSpec() {
        assert(refProjectDir.exists())
    }

    override fun deleteProjectDir() = false

    @Test
    fun `Avoids cleaning project directory after test`() {
        assert(project != null)
        assert(projectDir.exists())
    }
}