/*
 * Copyright 2013-2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.test

import org.gradle.api.logging.LogLevel
import java.io.File
import kotlin.reflect.KClass

/**
 * Base class which provides useful methods for testing a gradle plugin.
 *
 * <p>This is testing framework agnostic and can be either extended (see {@link BaseIntegrationSpec}) or composed, by
 * including it inside a test class as field.
 */
abstract class IntegrationBase {
    lateinit var projectDir: File
    lateinit var moduleName: String
    private var defaultLogLevel = LogLevel.LIFECYCLE
    val initScripts: ArrayList<File> = arrayListOf()
    var parallelEnabled = false

    open fun initialize(testClass: KClass<*>, testMethodName: String) = initialize(testClass, testMethodName, "nebulatest")

    open fun initialize(testClass: KClass<*>, testMethodName: String, baseFolderName: String) {
        projectDir = File("build/$baseFolderName/${testClass.qualifiedName}/${testMethodName.replace(Regex("\\W+"), "-")}").absoluteFile
        if (projectDir.exists())
            projectDir.deleteRecursively()
        projectDir.mkdirs()
        moduleName = findModuleName()
    }

    /**
     * Override to alter its value
     * @return
     */
    var logLevel: LogLevel
        get() {
            val levelFromEnv = System.getenv(LOGGING_LEVEL_ENV_VARIABLE) ?: return defaultLogLevel
            return LogLevel.valueOf(levelFromEnv.uppercase())
        }
        set(value) {
            defaultLogLevel = value
        }

    /* Setup */
    fun directory(path: String, baseDir: File = projectDir): File = File(baseDir, path).apply { mkdirs() }

    fun file(path: String, baseDir: File = projectDir): File {
        val splitted = path.split('/')
        val directory = if (splitted.size > 1) directory(splitted.dropLast(1).joinToString("/"), baseDir) else baseDir
        return File(directory, splitted.last()).apply { createNewFile() }
    }

    fun createFile(path: String, baseDir: File = projectDir): File {
        val file = file(path, baseDir)
        if (!file.exists()) {
            assert(file.parentFile.mkdirs() || file.parentFile.exists())
            file.createNewFile()
        }
        return file
    }

    fun writeHelloWorld(baseDir: File = projectDir) = writeHelloWorld("nebula", baseDir)

    fun writeHelloWorld(packageDotted: String, baseDir: File = projectDir) {
        writeJavaSourceFile("""
            package $packageDotted;
        
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello Integration Test");
                }
            }
            """.trimIndent(), "src/main/java", baseDir)
    }

    fun writeJavaSourceFile(source: String, projectDir: File = this.projectDir) =
        writeJavaSourceFile(source, "src/main/java", projectDir)

    fun writeJavaSourceFile(source: String, sourceFolderPath: String, projectDir: File = this.projectDir) {
        val javaFile = createFile(sourceFolderPath + '/' + fullyQualifiedName(source).replace(Regex("\\."), "/") + ".java", projectDir)
        javaFile.text = source
    }

    /**
     * Creates a passing unit test for testing your plugin.
     * @param baseDir the directory to begin creation from, defaults to projectDir
     */
    fun writeUnitTest(baseDir: File = projectDir) = writeTest("src/test/java/", "nebula", false, baseDir)

    /**
     * Creates a unit test for testing your plugin.
     * @param failTest true if you want the test to fail, false if the test should pass
     * @param baseDir the directory to begin creation from, defaults to projectDir
     */
    fun writeUnitTest(failTest: Boolean, baseDir: File = projectDir) = writeTest("src/test/java/", "nebula", failTest, baseDir)

    fun writeUnitTest(source: String, baseDir: File = projectDir) = writeJavaSourceFile(source, "src/test/java", baseDir)

    /**
     *
     * Creates a unit test for testing your plugin.
     * @param srcDir the directory in the project where the source file should be created.
     * @param packageDotted the package for the unit test class, written in dot notation (ex. - nebula.integration)
     * @param failTest true if you want the test to fail, false if the test should pass
     * @param baseDir the directory to begin creation from, defaults to projectDir
     */
    fun writeTest(srcDir: String, packageDotted: String, failTest: Boolean, baseDir: File = projectDir) {
        writeJavaSourceFile("""
            package $packageDotted;
            import org.junit.Test;
            import static org.junit.Assert.assertFalse;
    
            public class HelloWorldTest {
                @Test public void doesSomething() {
                    assertFalse( $failTest ); 
                }
            }
            """.trimIndent(), srcDir, baseDir)
    }

    private fun fullyQualifiedName(sourceStr: String): String {
        val pkgRegex = Regex("\\s*package\\s+([\\w.]+)")
        val pkg = pkgRegex.find(sourceStr)?.run { groupValues[1] + "." } ?: ""

        val classRegex = Regex("\\s*(class|interface)\\s+(\\w+)")
        return classRegex.find(sourceStr)!!.run { pkg + groupValues[2] }
    }

    /**
     * Creates a properties file to included as project resource.
     * @param srcDir the directory in the project where the source file should be created.
     * @param fileName to be used for the file, sans extension.  The .properties extension will be added to the name.
     * @param baseDir the directory to begin creation from, defaults to projectDir
     */
    fun writeResource(srcDir: String, fileName: String, baseDir: File = projectDir) {
        val path = "$srcDir/$fileName.properties"
        val resourceFile = createFile(path, baseDir)
        resourceFile.text = "firstProperty=foo.bar"
    }

    fun addResource(srcDir: String, filename: String, contents: String, baseDir: File = projectDir) {
        val resourceFile = createFile("$srcDir/$filename", baseDir)
        resourceFile.text = contents
    }

    fun findModuleName() = projectDir.name.replace(Regex("_\\d+"), "")

    fun calculateArguments(vararg args: String): List<String> = buildList {
        // Gradle will use these files name from the PWD, instead of the project directory. It's easier to just leave
        // them out and let the default find them, since we're not changing their default names.
        //arguments += '--build-file'
        //arguments += (buildFile.canonicalPath - projectDir.canonicalPath).substring(1)
        //arguments += '--settings-file'
        //arguments += (settingsFile.canonicalPath - projectDir.canonicalPath).substring(1)
        //arguments += '--no-daemon'
        when (logLevel) {
            LogLevel.INFO -> add("--info")
            LogLevel.DEBUG -> add("--debug")
            else -> {}
        }
        if (parallelEnabled)
            add("--parallel")
        add("--stacktrace")
        add("-Dorg.gradle.warning.mode=all")
        addAll(args)
        addAll(initScripts.map { "-I" + it.absolutePath })
    }

    companion object {

        private const val LOGGING_LEVEL_ENV_VARIABLE = "NEBULA_TEST_LOGGING_LEVEL"

        fun dependencies(buildFile: File,
                         vararg confs: String = listOf("compile", "testCompile", "implementation", "testImplementation", "api").toTypedArray()) =
            buildFile.readLines()
                    .map { it.trim() }
                    .filter { line -> confs.any { c -> line.startsWith(c) } }
                    .map { it.split(Regex("\\s+"))[1].replace(Regex("['\"]"), "") }
                    .sorted()

        fun checkOutput(output: String) {
            outputBuildScan(output)
            checkForMutableProjectState(output)
            checkForDeprecations(output)
        }

        fun outputBuildScan(output: String): Boolean {
            var foundPublishingLine = false
            return output.lines().any { line ->
                if (foundPublishingLine) {
                    if (line.startsWith("http"))
                        println("Build scan: $line")
                    else
                        println("Build scan was enabled but did not publish: $line")
                    true
                } else {
                    if (line == "Publishing build scan...")
                        foundPublishingLine = true
                    false
                }
            }
        }

        fun checkForDeprecations(output: String) {
            val deprecations = output.lines().filter {
                "has been deprecated and is scheduled to be removed in Gradle" in it ||
                "Deprecated Gradle features were used in this build" in it ||
                "has been deprecated. This is scheduled to be removed in Gradle" in it ||
                "This will fail with an error in Gradle" in it ||
                "This behaviour has been deprecated and is scheduled to be removed in Gradle" in it
            }
            // temporary for known issue with overwriting task
            // overridden task expected to not be needed in future version
            if (deprecations.size == 1 && "Creating a custom task named 'dependencyInsight' has been deprecated and is scheduled to be removed in Gradle 5.0." in deprecations.first())
                return
            if (System.getProperty("ignoreDeprecations") == null && deprecations.isNotEmpty())
                throw IllegalArgumentException("Deprecation warnings were found (Set the ignoreDeprecations system property during the test to ignore):\n" +
                                               deprecations.joinToString("\n") { " - $it" })
        }

        fun checkForMutableProjectState(output: String) {
            val mutableProjectStateWarnings = output.lines().filter {
                "was resolved without accessing the project in a safe manner" in it ||
                "This may happen when a configuration is resolved from a thread not managed by Gradle or from a different project" in it ||
                "was resolved from a thread not managed by Gradle." in it ||
                "was attempted from a context different than the project context" in it
            }

            if (System.getProperty("ignoreMutableProjectStateWarnings") == null && mutableProjectStateWarnings.isNotEmpty())
                throw IllegalArgumentException("Mutable Project State warnings were found (Set the ignoreMutableProjectStateWarnings system property during the test to ignore):\n" +
                                               mutableProjectStateWarnings.joinToString("\n") { " - $it" })
        }
    }
}
