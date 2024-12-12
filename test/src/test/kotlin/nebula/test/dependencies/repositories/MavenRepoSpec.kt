package nebula.test.dependencies.repositories

import nebula.test.dependencies.maven.Artifact
import nebula.test.dependencies.maven.Pom
import java.io.File
import kotlin.test.Test

class MavenRepoSpec {

    @Test
    fun `create repo`() {
        val repo = MavenRepo()
        val rootDir = "build/test/nebula.test.dependencies.repositories.MavenRepoSpec/create_repo/mavenrepo"
        repo.root = File(rootDir)
        if (repo.root.exists())
            repo.root.deleteRecursively()
        val example = Pom("test.nebula:ourbom:0.1.0", Artifact.Type.pom)
        val example2 = Pom("test.nebula:ourbom:0.2.0", Artifact.Type.pom)
        repo.poms += example
        repo.poms += example2

        repo.generate()

        val pom = File("$rootDir/test/nebula/ourbom/0.1.0/ourbom-0.1.0.pom")
        assert(pom.exists())

        val metadataFile = File("$rootDir/test/nebula/ourbom/maven-metadata.xml")
        assert(metadataFile.exists())

        operator fun String.get(name: String) = substringAfter("<$name>").substringBefore("</$name>")

        val metadata = metadataFile.readText()
        assert(metadata["groupId"] == "test.nebula")
        assert(metadata["artifactId"] == "ourbom")
        assert(metadata["versioning"]["latest"] == "0.2.0")
        assert(metadata["versioning"]["release"] == "0.2.0")
        val versions = metadata["versioning"]["versions"]
        assert("0.1.0" in versions && "0.2.0" in versions)
    }
}
