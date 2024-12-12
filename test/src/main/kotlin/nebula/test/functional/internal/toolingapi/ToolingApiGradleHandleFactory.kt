package nebula.test.functional.internal.toolingapi

import nebula.test.div
import nebula.test.functional.internal.GradleHandle
import nebula.test.functional.internal.GradleHandleBuildListener
import nebula.test.functional.internal.GradleHandleFactory
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import java.io.File
import java.net.URI
import java.util.Properties

import java.util.concurrent.TimeUnit

class ToolingApiGradleHandleFactory(private val fork: Boolean,
                                    private val version: String?,
                                    private val daemonMaxIdleTimeInSeconds: Int? = null) : GradleHandleFactory {

    override fun start(dir: File, arguments: List<String>): GradleHandle = start(dir, arguments, emptyList())

    override fun start(dir: File, arguments: List<String>, jvmArguments: List<String>): GradleHandle {

        val connector = createGradleConnector(dir)

        val forkedProcess = forkedProcess

        // Allow for in-process debugging
        //        connector.embedded(!forkedProcess) //TODO

        //        if (daemonMaxIdleTimeInSeconds != null)
        //            connector.daemonMaxIdleTime(daemonMaxIdleTimeInSeconds, TimeUnit.SECONDS)

        val connection = connector.connect()
        val launcher = createBuildLauncher(connection, arguments, jvmArguments)
        return createGradleHandle(connector, connection, launcher, forkedProcess)
    }

    private fun createGradleConnector(projectDir: File): GradleConnector {
        val connector = GradleConnector.newConnector()
        connector.forProjectDirectory(projectDir)
        configureGradleVersion(connector, projectDir)
        return connector
    }

    private fun configureGradleVersion(connector: GradleConnector, projectDir: File) {
        if (version != null)
            connector.useGradleVersion(version)
        else
            configureWrapperDistributionIfUsed(connector, projectDir)
    }

    private val forkedProcess
        get() = if (fork) true else System.getProperty(FORK_SYS_PROP).toBoolean()

    private fun createGradleHandle(connector: GradleConnector, connection: ProjectConnection,
                                   launcher: BuildLauncher, forkedProcess: Boolean): GradleHandle {

        val toolingApiBuildListener = ToolingApiBuildListener(connection)
        val buildLauncherBackedGradleHandle = BuildLauncherBackedGradleHandle(connector, launcher, forkedProcess)
        buildLauncherBackedGradleHandle.registerBuildListener(toolingApiBuildListener)
        return buildLauncherBackedGradleHandle
    }

    private class ToolingApiBuildListener(private val connection: ProjectConnection) : GradleHandleBuildListener {
        override fun buildStarted() {}
        override fun buildFinished() = connection.close()
    }

    companion object {
        const val FORK_SYS_PROP = "nebula.test.functional.fork"

        private fun configureWrapperDistributionIfUsed(connector: GradleConnector, projectDir: File) {
            // Search above us, in the project that owns the test
            var target: File? = projectDir.absoluteFile
            while (target != null) {
                val distribution = prepareDistributionURI(target)
                if (distribution != null) {
                    connector.useDistribution(distribution)
                    return
                }
                target = target.parentFile
            }
        }

        // Translated from org.gradle.wrapper.WrapperExecutor to avoid coupling to Gradle API
        private fun prepareDistributionURI(target: File): URI? {
            val propertiesFile = target / "gradle/wrapper/gradle-wrapper.properties"
            if (propertiesFile.exists()) {
                val properties = Properties()
                propertiesFile.inputStream().use { properties.load(it) }
                val source = URI(properties.getProperty("distributionUrl"))
                return when (source.scheme) {
                    null -> (propertiesFile.parentFile / source.schemeSpecificPart).toURI()
                    else -> source
                }
            }
            return null
        }

        private fun createBuildLauncher(connection: ProjectConnection,
                                        arguments: List<String>,
                                        jvmArguments: List<String>): BuildLauncher {

            val launcher = connection.newBuild()
            launcher.withArguments(arguments)
            launcher.setJvmArguments(jvmArguments)
            return launcher
        }
    }
}
