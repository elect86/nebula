/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.test

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

/**
 * Create some basic tests that all plugins should pass
 */
@CompileStatic
abstract class PluginProjectSpec extends ProjectSpec {
    abstract String getPluginName()

    def 'apply does not throw exceptions'() {
        when:
        project.apply plugin: pluginName

        then:
        noExceptionThrown()
    }

    def 'apply is idempotent'() {
        when:
        project.apply plugin: pluginName
        project.apply plugin: pluginName

        then:
        noExceptionThrown()
    }

    def 'apply is fine on all levels of multiproject'() {
        def sub = createSubproject(project, 'sub')
        project.subprojects.add(sub)

        when:
        project.apply plugin: pluginName
        sub.apply plugin: pluginName

        then:
        noExceptionThrown()
    }

    def 'apply to multiple subprojects'() {
        def subprojectNames = ['sub1', 'sub2', 'sub3']

        subprojectNames.each { subprojectName ->
            def subproject = createSubproject(project, subprojectName)
            project.subprojects.add(subproject)
        }

        when:
        project.apply plugin: pluginName

        subprojectNames.each { subprojectName ->
            def subproject = project.subprojects.find { it.name == subprojectName }
            subproject.apply plugin: pluginName
        }

        then:
        noExceptionThrown()
    }

    Project createSubproject(Project parentProject, String name) {
        ProjectBuilder.builder().withName(name).withProjectDir(new File(projectDir, name)).withParent(parentProject).build()
    }
}
