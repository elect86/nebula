package nebula.test;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

class FakePlugin: Plugin<Project> {

    override fun apply(target: Project) {
        // Intentionally empty
    }
}