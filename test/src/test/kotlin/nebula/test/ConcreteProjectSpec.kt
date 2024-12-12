package nebula.test

import org.gradle.api.internal.project.DefaultProject
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test

class ConcreteProjectSpec : ProjectSpec() {

    @Test
    fun `has Project`() {

        // lateinit, but we cant access `this::project.isInitialized` because
        // Backing field of 'var project: Project' is not accessible at this point.
        assert(project != null)
        projectDir.exists()
    }

    @Test
    fun `can evaluate`() {

        val signal = AtomicBoolean(false)
        project.afterEvaluate {
            signal.getAndSet(true)
        }

        (project as DefaultProject).evaluate()

        //        then:
        //        noExceptionThrown()
        assert(signal.get())
    }
}