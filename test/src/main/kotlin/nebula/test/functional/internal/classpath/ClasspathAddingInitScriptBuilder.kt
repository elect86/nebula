package nebula.test.functional.internal.classpath

import nebula.test.functional.ClasspathFilter
import org.gradle.internal.ErroringAction
import org.gradle.internal.IoActions
import org.gradle.internal.classloader.ClasspathUtil
import org.gradle.internal.classpath.ClassPath
import org.gradle.util.internal.TextUtil
import java.io.File
import java.io.Writer
import java.net.URL

import java.util.function.Predicate

object ClasspathAddingInitScriptBuilder {

    fun build(initScriptFile: File, classLoader: ClassLoader, classpathFilter: ClasspathFilter) =
        build(initScriptFile, getClasspathAsFiles(classLoader, classpathFilter));

    fun build(initScriptFile: File, classpath: List<File>) {
        IoActions.writeTextFile(initScriptFile) {
            it.write("allprojects {\n")
            it.write("  buildscript {\n")
            it.write("    dependencies {\n")
            it.writeClasspath(classpath)
            it.write("    }\n")
            it.write("  }\n")
            it.write("}\n")
            it.write("initscript {\n")
            it.write("  dependencies {\n")
            it.writeClasspath(classpath)
            it.write("  }\n")
            it.write("}\n")
        }
    }

    fun Writer.writeClasspath(classpath: List<File>) {
        for (file in classpath)
        // Commons-lang 2.4 does not escape forward slashes correctly, https://issues.apache.org/jira/browse/LANG-421
            write(String.format("      classpath files('%s')\n", TextUtil.escapeString(file.absolutePath)));
    }

    fun getClasspathAsFiles(classLoader: ClassLoader, classpathFilter: ClasspathFilter): List<File> {
        val classpathUrls = getClasspathUrls(classLoader)
        return classpathUrls.filter { classpathFilter(it) }.map { File(it.toURI()) }
    }

    @Suppress("unchecked_cast")
    private fun getClasspathUrls(classLoader: ClassLoader): List<URL> {
        val cp = ClasspathUtil.getClasspath(classLoader)
        if (cp is List<*> && cp.all { it is URL })
            return cp as List<URL>
        if (cp is ClassPath) // introduced by gradle/gradle@0ab8bc2
            return cp.asURLs
        throw IllegalStateException("Unable to extract classpath urls from type ${cp::class.qualifiedName}")
    }
}
