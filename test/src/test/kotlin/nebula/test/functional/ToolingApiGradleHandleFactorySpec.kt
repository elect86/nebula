package nebula.test.functional

import nebula.test.functional.internal.GradleHandle
import nebula.test.functional.internal.GradleHandleFactory
import nebula.test.functional.internal.toolingapi.ToolingApiGradleHandleFactory
import java.io.File
import kotlin.test.Test

class ToolingApiGradleHandleFactorySpec {

    val projectDir = File("myProject")

    @Test
    fun `Creates embedded handle if requested through constructor`() {

        val gradleHandleFactory = ToolingApiGradleHandleFactory(false, null)
        val gradleHandle = gradleHandleFactory.start(projectDir, emptyList())

//        gradleHandle // we return non-null
        assert(!gradleHandle.forkedProcess)
    }

    @Test
    fun `Creates forked handle if requested through constructor`() {

        val gradleHandleFactory = ToolingApiGradleHandleFactory(true, null)
        val gradleHandle = gradleHandleFactory.start(projectDir, emptyList())

//        gradleHandle
        assert(gradleHandle.forkedProcess)
    }

    @Test
    fun `Creates forked handle if requested through system property`() {

        System.setProperty(ToolingApiGradleHandleFactory.FORK_SYS_PROP, true.toString())

        val gradleHandleFactory = ToolingApiGradleHandleFactory(false, null)
        val gradleHandle = gradleHandleFactory.start(projectDir, emptyList())

//        gradleHandle
        assert(gradleHandle.forkedProcess)

        System.clearProperty(ToolingApiGradleHandleFactory.FORK_SYS_PROP)
    }
}
