package nebula.test

import org.gradle.util.GradleVersion
import java.io.File

var File.text: String
    get() = readText()
    set(value) = writeText(value)

operator fun File.div(text: String) = resolve(text)
operator fun File.plusAssign(text: String) = appendText(text)

fun GradleVersion(version: String) = GradleVersion.version(version)