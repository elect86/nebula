package nebula.test

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test

class BuildScanIntegrationSpec : IntegrationTestKitSpec() {

    val origOut = System.out
    val out = ByteArrayOutputStream()

    @BeforeEach
    fun setup() {
        val settings = projectDir / "settings.gradle"
        settings.createNewFile()
        settings += """
            |gradleEnterprise {
            |   buildScan {
            |       termsOfServiceUrl = 'https://gradle.com/terms-of-service'
            |       termsOfServiceAgree = 'yes'
            |   }
            |}""".trimMargin()
        val printStream = PrintStream(out)
        System.setOut(printStream)
    }

    @AfterEach
    override fun cleanup() = System.setOut(origOut)

    @Test
    fun `build scan url is reported in test output`() {

        runTasks("help", "--scan")

        assert("Build scan" in out.toString("UTF-8"))
    }
}
