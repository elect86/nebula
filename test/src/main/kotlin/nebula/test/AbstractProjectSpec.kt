package nebula.test

import nebula.test.multiproject.MultiProjectHelper
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testfixtures.internal.ProjectBuilderImpl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import java.io.File

/**
 * Setup a temporary project on the fly, uses Spock.
 *
 * Caveat, this is ONLY setting up the Project data structure, and not running through the completely lifecycle, Like to
 * see http://issues.gradle.org/browse/GRADLE-1619
 *
 * Its value lays in being able to execute method with a proper Project object, which can flush out most groovy functions,
 * finding basic compiler like issues.
 */
abstract class AbstractProjectSpec {

    lateinit var ourProjectDir: File

    lateinit var canonicalName: String
    lateinit var project: Project
    lateinit var helper: MultiProjectHelper

    @BeforeEach
    fun setup(testInfo: TestInfo) {
        val methodName = testInfo.testMethod.get().name
        ourProjectDir = File("build/nebulatest/${this::class.qualifiedName}/${methodName.replace(Regex("\\W+"), "-")}").absoluteFile
        if (ourProjectDir.exists())
            ourProjectDir.deleteRecursively()
        ourProjectDir.mkdirs()
        canonicalName = methodName.replace(' ', '-')
        project = ProjectBuilder.builder().withName(canonicalName).withProjectDir(ourProjectDir).build()
        helper = MultiProjectHelper(project)
    }

    @AfterEach
    fun cleanup() {
        if (deleteProjectDir())
            ourProjectDir.deleteRecursively()
    }

    /**
     * Determines if project directory should be deleted after a test was executed. By default the logic checks for
     * the system property "cleanProjectDir". If the system property is provided and has the value "true", the project
     * directory is deleted. If this system property is not provided, the project directory is always deleted. Test
     * classes that inherit from this class, can override the method to provide custom logic.
     *
     * @return Flag
     */
    open fun deleteProjectDir(): Boolean = System.getProperty(CLEAN_PROJECT_DIR_SYS_PROP)?.toBoolean() ?: true

    infix fun addSubproject(subprojectName: String): Project = helper.addSubproject(subprojectName)

    infix fun addSubprojectWithDirectory(subprojectName: String): Project = helper.addSubprojectWithDirectory(subprojectName)

    companion object {
        const val CLEAN_PROJECT_DIR_SYS_PROP = "cleanProjectDir"
    }
}

