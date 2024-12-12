/*
 * Copyright 2015-2017 Netflix, Inc.
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
package nebula.test.functional

import nebula.test.div
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.condition.EnabledOnJre
import org.junit.jupiter.api.condition.JRE
import org.junit.jupiter.api.extension.ExtendWith
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension
import java.io.File
import java.net.URI
import java.net.URL
import kotlin.test.Test


/**
 * Tests for predicates that live on {@link GradleRunner}.
 */
@ExtendWith(SystemStubsExtension::class)
class GradleRunnerSpec {

    lateinit var classpath: List<URL>

    val workDir = File(System.getProperty("user.dir"))
    val siblingDir = workDir.parentFile / "sibling"
    val sharedDependencyCache = workDir.parentFile / "sharedDependencyCache"
    val testDistributionWorkspace = workDir.parentFile / "gradle-enterprise-test-distribution-agent-workspace"

    @BeforeEach
    fun setup() {
        // Partial real-world classpath from a IntegrationSpec launch, only the workDir/siblingDir paths matter, otherwise these are just string comparisons
        val classpathUris = listOf(
            "file:/Applications/IntelliJ%20IDEA%2015%20EAP.app/Contents/lib/serviceMessages.jar",
            "file:/Applications/IntelliJ%20IDEA%2015%20EAP.app/Contents/lib/idea_rt.jar",
            "file:/Applications/IntelliJ%20IDEA%2015%20EAP.app/Contents/plugins/junit/lib/junit-rt.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/lib/ant-javafx.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/lib/dt.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/lib/javafx-doclet.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/lib/javafx-mx.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/lib/jconsole.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/lib/sa-jdi.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/lib/tools.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/charsets.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/deploy.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/htmlconverter.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/javaws.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/jce.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/jfr.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/jfxrt.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/jsse.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/management-agent.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/plugin.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/resources.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/rt.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/ext/dnsns.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/ext/localedata.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/ext/sunec.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar",
            "file:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/ext/zipfs.jar",

            // The project that is being tested always appears as follows:
            workDir.resolve("build/classes/test/").toURI().toString(),
            workDir.resolve("build/classes/main/").toURI().toString(),
            workDir.resolve("build/resources/test/").toURI().toString(),
            workDir.resolve("build/resources/main/").toURI().toString(),

            // when launched from IDE, project dependencies appear this way:
            siblingDir.resolve("build/classes/test/").toURI().toString(),
            siblingDir.resolve("build/classes/main/").toURI().toString(),
            siblingDir.resolve("build/resources/test/").toURI().toString(),
            siblingDir.resolve("build/resources/main/").toURI().toString(),

            // when launched from IntelliJ, and not delegating to Gradle for compilation, project dependencies appear this way:
            siblingDir.resolve("out/integTest/classes").toURI().toString(),
            siblingDir.resolve("out/test/classes").toURI().toString(),
            siblingDir.resolve("out/test/resources").toURI().toString(),
            siblingDir.resolve("out/production/classes").toURI().toString(),
            siblingDir.resolve("out/production/resources").toURI().toString(),

            // when launched from Gradle, project dependencies appear as jars:
            siblingDir.resolve("build/libs/repos-4.0.0.jar").toURI().toString(),

            workDir.resolve(".gradle/caches/modules-2/files-2.1/org.spockframework/spock-core/1.0-groovy-2.3/762fbf6c5f24baabf9addcf9cf3647151791f7eb/spock-core-1.0-groovy-2.3.jar").toURI().toString(),
            workDir.resolve(".gradle/caches/modules-2/files-2.1/cglib/cglib-nodep/2.2.2/d456bb230c70c0b95c76fb28e429d42f275941/cglib-nodep-2.2.2.jar").toURI().toString(),
            workDir.resolve(".gradle/caches/modules-2/files-2.1/commons-lang/commons-lang/2.6/ce1edb914c94ebc388f086c6827e8bdeec71ac2/commons-lang-2.6.jar").toURI().toString(),
            workDir.resolve(".gradle/caches/modules-2/files-2.1/junit/junit/4.12/2973d150c0dc1fefe998f834810d68f278ea58ec/junit-4.12.jar").toURI().toString(),
            workDir.resolve(".gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar").toURI().toString(),
            workDir.resolve(".gradle/wrapper/dists/gradle-2.2.1-bin/3rn023ng4778ktj66tonmgpbv/gradle-2.2.1/lib/gradle-core-2.2.1.jar").toURI().toString(),
            workDir.resolve(".gradle/wrapper/dists/gradle-2.2.1-bin/3rn023ng4778ktj66tonmgpbv/gradle-2.2.1/lib/groovy-all-2.3.6.jar").toURI().toString(),
            workDir.resolve(".gradle/wrapper/dists/gradle-2.2.1-bin/3rn023ng4778ktj66tonmgpbv/gradle-2.2.1/lib/asm-all-5.0.3.jar").toURI().toString(),
            workDir.resolve(".gradle/wrapper/dists/gradle-2.2.1-bin/3rn023ng4778ktj66tonmgpbv/gradle-2.2.1/lib/ant-1.9.3.jar").toURI().toString(),
            workDir.resolve(".gradle/wrapper/dists/gradle-2.2.1-bin/3rn023ng4778ktj66tonmgpbv/gradle-2.2.1/lib/commons-collections-3.2.1.jar").toURI().toString(),
            workDir.resolve(".gradle/wrapper/dists/gradle-2.2.1-bin/3rn023ng4778ktj66tonmgpbv/gradle-2.2.1/lib/commons-io-1.4.jar").toURI().toString(),

            // [Kotlin] removing leading slashed to avoid absolute
            sharedDependencyCache.resolve("modules-2/files-2.1/junit/junit/4.13/2973d150c0dc1fefe998f834810d68f278ea58ec/junit-4.13.jar").toURI().toString(),
            testDistributionWorkspace.resolve("modules-2/files-2.1/junit/junit/4.12/2973d150c0dc1fefe998f834810d68f278ea58dc/junit-4.12.jar").toURI().toString(),
            testDistributionWorkspace.resolve("modules-2/files-2.1/commons-lang/commons-lang/2.2/ce1edb914c94ebc388f086c6827e8bdeec71ac1/commons-lang-2.2.jar").toURI().toString(),

            File(System.getProperty("user.home"), ".m2/repository/com/netflix/genie/genie-common/4.0.0-SNAPSHOT/genie-common-4.0.0-SNAPSHOT.jar").toURI().toString()
        )
        classpath = classpathUris.map { URI(it).toURL() }
    }

    @Test
    fun `gradle distribution predicate matches expected files`() {
        val filtered = classpath.filter(GradleRunnerFactory.CLASSPATH_GRADLE_CACHE)
        assert(filtered.size == 4)
    }

    @SystemStub
    private lateinit var environmentVariables: EnvironmentVariables

    @Test
    @EnabledOnJre(JRE.JAVA_8)
    fun `gradle distribution predicate matches expected files with GRADLE_RO_DEP_CACHE support`() {

        environmentVariables["GRADLE_RO_DEP_CACHE"] = sharedDependencyCache.absolutePath

        val filtered = environmentVariables.execute<List<URL>> {
            classpath.filter { GradleRunnerFactory.CLASSPATH_GRADLE_CACHE(it) }
        }
        assert(filtered.size == 4)
        assert(filtered.any { "commons-lang-2.6" in it.file })
    }

    @Test
    fun `gradle distribution predicate matches expected files with test distribution folder support`() {

        val filtered = classpath.filter(GradleRunnerFactory.CLASSPATH_GRADLE_CACHE)

        assert(filtered.size == 4)
        assert(filtered.any { "commons-lang-2.2" in it.file })
    }

    @Test
    fun `jvm predicate matches expected files`() {
        val filtered = classpath.filter(GradleRunnerFactory.CLASSPATH_PROJECT_DIR)
        assert(filtered.size == 15)
    }

    @Test
    fun `project dependencies matches expected files`() {
        val filtered = classpath . filter(GradleRunnerFactory.CLASSPATH_PROJECT_DEPENDENCIES)
        assert(filtered.size == 14)
    }

    @Test
    fun `maven local dependencies matches expected files`() {
        val filtered = classpath . filter(GradleRunnerFactory.MAVEN_LOCAL)
        assert(filtered.size == 1)
        assert(filtered.all { ".m2/repository" in it.file })
    }

    @Test
    fun `default classpath matches only application class paths and dependencies`() {
        val filtered = classpath . filter(GradleRunnerFactory.CLASSPATH_DEFAULT)
        assert(filtered.size == 27)
    }
}
