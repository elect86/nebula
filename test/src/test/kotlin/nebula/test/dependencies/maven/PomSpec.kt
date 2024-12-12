package nebula.test.dependencies.maven

import kotlin.test.Test

class PomSpec {

    @Test
    fun `generate basic pom`() {
        val pom = Pom("nebula.test:basic:0.1.0")

        val pomXml = pom.generate()

        val expected = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <modelVersion>4.0.0</modelVersion>
              <groupId>nebula.test</groupId>
              <artifactId>basic</artifactId>
              <version>0.1.0</version>
            </project>""".trimIndent()
        assert(pomXml == expected)
    }

    @Test
    fun `generate bom`() {
        val pom = Pom("nebula.test:basic:0.1.0", Artifact.Type.pom)
        pom addManagementDependency "foo:bar:1.2.3"

        val pomXml = pom.generate()

        val expected = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <modelVersion>4.0.0</modelVersion>
              <groupId>nebula.test</groupId>
              <artifactId>basic</artifactId>
              <version>0.1.0</version>
              <packaging>pom</packaging>
              <dependencyManagement>
                <dependencies>
                  <dependency>
                    <groupId>foo</groupId>
                    <artifactId>bar</artifactId>
                    <version>1.2.3</version>
                  </dependency>
                </dependencies>
              </dependencyManagement>
            </project>""".trimIndent()
        assert(pomXml == expected)
    }

    @Test
    fun `generate pom with dependency`() {
        val pom = Pom("nebula.test:basic:0.1.0")
        pom.addDependency("foo:bar:1.2.3")
        pom.addDependency(Artifact("baz:qux:2.0.1"))

        val pomXml = pom.generate()

        val expected = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <modelVersion>4.0.0</modelVersion>
              <groupId>nebula.test</groupId>
              <artifactId>basic</artifactId>
              <version>0.1.0</version>
              <dependencies>
                <dependency>
                  <groupId>baz</groupId>
                  <artifactId>qux</artifactId>
                  <version>2.0.1</version>
                </dependency>
                <dependency>
                  <groupId>foo</groupId>
                  <artifactId>bar</artifactId>
                  <version>1.2.3</version>
                </dependency>
              </dependencies>
            </project>""".trimIndent()
        assert(pomXml == expected)
    }
}
