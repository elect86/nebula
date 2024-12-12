package nebula.test.functional.internal.classpath

import nebula.test.div
import nebula.test.functional.ClasspathFilter
import nebula.test.functional.internal.GradleHandle
import nebula.test.functional.internal.GradleHandleFactory
import java.io.File

class ClasspathInjectingGradleHandleFactory(val classLoader: ClassLoader,
                                            val delegateFactory: GradleHandleFactory,
                                            val classpathFilter: ClasspathFilter) : GradleHandleFactory {

    override fun start(dir: File, arguments: List<String>): GradleHandle = start(dir, arguments, emptyList())

    override fun start(dir: File, arguments: List<String>, jvmArguments: List<String>): GradleHandle {
        val testKitDir = dir / ".gradle-test-kit"
        if (!testKitDir.exists())
            testKitDir.mkdirs()

        val initScript = testKitDir / "init.gradle"
        ClasspathAddingInitScriptBuilder.build(initScript, classLoader, classpathFilter)

        val ammendedArguments = buildList<String>(arguments.size + 2) {
            add("--init-script")
            add(initScript.absolutePath)
            addAll(arguments)
        }
        return delegateFactory.start(dir, ammendedArguments, jvmArguments)
    }
}
