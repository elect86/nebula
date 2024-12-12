package nebula.test

import java.io.File

/**
 * Created by rspieldenner on 3/25/15.
 */
abstract class ProjectSpec : AbstractProjectSpec() {
    val projectDir: File
        get() = ourProjectDir
}
