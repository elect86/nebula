package nebula.test.dependencies

class DependencyGraphBuilder {

    val modules = mutableMapOf<String, DependencyGraphNode>()

    fun addModules(vararg coordinates: String): DependencyGraphBuilder {
        for (coordinate in coordinates)
            addModule(coordinate)
        return this
    }

    fun addModule(coordinate: String): DependencyGraphBuilder {
        val parts = coordinate.trim().split(':')
        return addModule(parts[0], parts[1], parts[2], parts.getOrNull(3))
    }

    fun addModule(group: String, artifact: String, version: String, status: String? = DEFAULT_STATUS): DependencyGraphBuilder {
        val key = "$group:$artifact:$version"
        modules[key] = DependencyGraphNode(Coordinate(group, artifact, version), status = status ?: DEFAULT_STATUS)
        return this
    }

    fun addModules(vararg nodes: DependencyGraphNode) {
        for (node in nodes)
            addModule(node)
    }

    fun addModule(node: DependencyGraphNode): DependencyGraphBuilder {
        modules[node.coordinate.toString()] = node

        node.dependencies.forEach {
            if (!modules.containsKey(it.toString()))
                modules[it.toString()] = DependencyGraphNode(it)
        }
        return this
    }

    operator fun plusAssign(node: String) {
        addModule(node)
    }

    fun build() = DependencyGraph("nodes" to modules.values.toList())

    companion object {
        private val DEFAULT_STATUS = "integration"
    }
}
