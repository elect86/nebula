package nebula.test.functional.internal

import java.io.File

interface GradleHandleFactory {

    fun start(dir: File, arguments: List<String>): GradleHandle

    fun start(dir: File, arguments: List<String>, jvmArguments: List<String>): GradleHandle
}
