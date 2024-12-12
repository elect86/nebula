package nebula.test.multiproject

import java.io.File

data class MultiProjectIntegrationInfo(val name: String,
                                       val directory: File,
                                       val buildGradle: File)
