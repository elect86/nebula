package nebula.test.dependencies

class ModuleBuilder(val module: Coordinate) {

    var status = "integration"
    val dependencies: ArrayList<Coordinate> = arrayListOf()

    constructor(group: String, artifact: String, version: String) : this(Coordinate(group, artifact, version))

    fun addDependencies(vararg dependencies: String): ModuleBuilder {
        for (dependency in dependencies)
            addDependency(dependency)
        return this
    }

    fun addDependency(dependency: String): ModuleBuilder {
        val (group, artifact, version) = dependency.split(':')
        dependencies += Coordinate(group, artifact, version)
        return this
    }

    fun addDependency(group: String, artifact: String, version: String): ModuleBuilder {
        dependencies += Coordinate(group, artifact, version)
        return this
    }

    fun setStatus(status: String): ModuleBuilder {
        this.status = status
        return this
    }

    fun build() = DependencyGraphNode(module, dependencies, status)
}

fun ModuleBuilder(coordinate: String): ModuleBuilder {
    val (group, artifact, version) = coordinate.split(':')
    return ModuleBuilder(Coordinate(group, artifact, version))
}