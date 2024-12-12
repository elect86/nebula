package nebula.test

import java.io.File
import kotlin.test.Test

class ProjectDirCleanupProjectSpec: ProjectSpec() {

    lateinit var refProjectDir: File

    fun setup() {
        refProjectDir = projectDir
    }

    fun cleanupSpec() {
        assert(!refProjectDir.exists())
    }

    @Test
    fun `Cleans project directory after test`() {
        assert(project != null)
        assert(projectDir.exists ())
    }
}
