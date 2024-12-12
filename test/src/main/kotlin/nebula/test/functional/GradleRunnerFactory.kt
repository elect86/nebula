package nebula.test.functional

import nebula.test.functional.internal.DefaultGradleRunner
import nebula.test.functional.internal.GradleHandleFactory
import nebula.test.functional.internal.classpath.ClasspathInjectingGradleHandleFactory
import nebula.test.functional.internal.toolingapi.ToolingApiGradleHandleFactory
import java.io.File
import java.net.URL

object GradleRunnerFactory {

    fun createTooling(fork: Boolean = false,
                      version: String? = null,
                      daemonMaxIdleTimeInSeconds: Int? = null,
                      classpathFilter: ClasspathFilter? = null): GradleRunner {

        val toolingApiHandleFactory = ToolingApiGradleHandleFactory(fork, version, daemonMaxIdleTimeInSeconds)
        return create(toolingApiHandleFactory, classpathFilter ?: CLASSPATH_DEFAULT)
    }

    fun create(handleFactory: GradleHandleFactory, classpathFilter: ClasspathFilter? = null): GradleRunner {
        // TODO: Which class would be attached to the right classloader? Is using something from the test kit right?
        val sourceClassLoader = GradleRunnerFactory::class.java.classLoader
        return create(handleFactory, sourceClassLoader, classpathFilter ?: CLASSPATH_DEFAULT)
    }

    fun create(handleFactory: GradleHandleFactory,
               sourceClassLoader: ClassLoader,
               classpathFilter: ClasspathFilter): GradleRunner {

        val classpathInjectingHandleFactory = ClasspathInjectingGradleHandleFactory(sourceClassLoader, handleFactory, classpathFilter)
        return DefaultGradleRunner (classpathInjectingHandleFactory)
    }

    // These predicates are here, instead of on GradleRunnerFactory due to a Groovy static compiler bug (https://issues.apache.org/jira/browse/GROOVY-7159)

    const val SHARED_DEPENDENCY_CACHE_ENVIRONMENT_VARIABLE = "GRADLE_RO_DEP_CACHE"

    val CLASSPATH_GRADLE_CACHE: ClasspathFilter = { url ->
        fun isTestingFramework(url: URL) = "spock-" in url.path || "junit-" in url.path

        val gradleSharedDependencyCache = System.getenv(SHARED_DEPENDENCY_CACHE_ENVIRONMENT_VARIABLE)
        val cachedModule = "/caches/modules-" in url.path
        val readOnlyCachedModule = gradleSharedDependencyCache != null && gradleSharedDependencyCache.isNotEmpty() &&
                                   "$gradleSharedDependencyCache/modules-" in url.path
        val testDistributionOrphanedFile = "/orphan-files/" in url.path // test distribution orphans read-only dependency cache files
        val testDistributionFolder = "/gradle-enterprise-test-distribution-agent-workspace/" in url.path // test distribution orphans read-only dependency cache files
        (cachedModule || readOnlyCachedModule || testDistributionOrphanedFile || testDistributionFolder) && !isTestingFramework(url)
    }

    val MAVEN_LOCAL: ClasspathFilter = { url ->
        val m2RepositoryPrefix = System.getProperty("user.home") + "/.m2/repository"
        m2RepositoryPrefix in url.path
    }

    val CLASSPATH_PROJECT_DIR: ClasspathFilter = { url ->
        val userDir = File(System.getProperty("user.dir"))
        url.path.startsWith(userDir.toURI().toURL().path)
    }

    val CLASSPATH_PROJECT_DEPENDENCIES: ClasspathFilter = { url ->
        "/build/classes" in url.path || "/build/resources" in url.path || "/build/libs" in url.path || "/out/" in url.path
    }

    /**
     * Attempts to provide a classpath that approximates the 'normal' Gradle runtime classpath. Use {@link #CLASSPATH_ALL}
     * to default to pre-2.2.2 behaviour.
     */
    val CLASSPATH_DEFAULT: ClasspathFilter = { url ->
        CLASSPATH_PROJECT_DIR(url) || CLASSPATH_GRADLE_CACHE(url) || CLASSPATH_PROJECT_DEPENDENCIES(url) || MAVEN_LOCAL(url)
    }

    /**
     * Accept all URLs. Provides pre-2.2.2 behaviour.
     */
    val CLASSPATH_ALL: ClasspathFilter = { true }
}

typealias ClasspathFilter = (URL) -> Boolean