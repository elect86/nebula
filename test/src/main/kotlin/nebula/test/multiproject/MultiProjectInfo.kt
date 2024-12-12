package nebula.test.multiproject

import org.gradle.api.Project
import java.io.File

data class MultiProjectInfo(val name: String,
                            val project: Project,
                            val parent: Project,
                            val directory: File? = null)
