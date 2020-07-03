package de.blox.graphview

class Graph {
    private val _nodes = arrayListOf<Node>()
    private val _edges = arrayListOf<Edge>()
    private val graphObserver = arrayListOf<GraphObserver>()
    val nodes: List<Node> = _nodes
    val edges: List<Edge> = _edges

    private var isTree = false

    val nodeCount: Int
        get() = _nodes.size

    fun addNode(node: Node) {
        if (node !in _nodes) {
            _nodes.add(node)
            notifyGraphObserver()
        }
    }

    fun addNodes(vararg nodes: Node) = nodes.forEach { addNode(it) }

    fun removeNode(node: Node) {
        if (!_nodes.contains(node)) {
            throw IllegalArgumentException("Unable to find node in graph.")
        }

        if (isTree) {
            for (n in successorsOf(node)) {
                removeNode(n)
            }
        }

        _nodes.remove(node)

        val iterator = _edges.iterator()
        while (iterator.hasNext()) {
            val (source, destination) = iterator.next()
            if (source == node || destination == node) {
                iterator.remove()
            }
        }

        notifyGraphObserver()
    }

    fun removeNodes(vararg nodes: Node) = nodes.forEach { removeNode(it) }

    fun addEdge(source: Node, destination: Node): Edge {
        val edge = Edge(source, destination)
        addEdge(edge)

        return edge
    }

    fun addEdge(edge: Edge) {
        addNode(edge.source)
        addNode(edge.destination)

        if (edge !in _edges) {
            _edges.add(edge)
            notifyGraphObserver()
        }
    }

    fun addEdges(vararg edges: Edge) = edges.forEach { addEdge(it) }

    fun removeEdge(edge: Edge) = _edges.remove(edge)

    fun removeEdges(vararg edges: Edge) = edges.forEach { removeEdge(it) }

    fun removeEdge(predecessor: Node, current: Node) {
        val iterator = _edges.iterator()
        while (iterator.hasNext()) {
            val (source, destination) = iterator.next()
            if (source == predecessor && destination == current) {
                iterator.remove()
            }
        }
    }

    fun hasNodes(): Boolean = _nodes.isNotEmpty()

    fun getNodeAtPosition(position: Int): Node {
        if (position < 0) {
            throw IllegalArgumentException("position can't be negative")
        }

        val size = _nodes.size
        if (position >= size) {
            throw IndexOutOfBoundsException("Position: $position, Size: $size")
        }

        return _nodes[position]
    }

    fun getEdgeBetween(source: Node, destination: Node): Edge? =
        _edges.find { it.source == source && it.destination == destination }

    fun hasSuccessor(node: Node): Boolean = _edges.any { it.source == node }

    fun successorsOf(node: Node) =
        _edges.filter { it.source == node }.map { edge -> edge.destination }

    fun hasPredecessor(node: Node): Boolean = _edges.any { it.destination == node }

    fun predecessorsOf(node: Node) =
        _edges.filter { it.destination == node }.map { edge -> edge.source }

    operator fun contains(node: Node): Boolean = _nodes.contains(node)
    operator fun contains(edge: Edge): Boolean = _edges.contains(edge)

    fun containsData(data: Any) = _nodes.any { it.data == data }

    fun getNodeAtPosition(data: Any) = _nodes.find { node -> node.data == data }

    fun getOutEdges(node: Node): List<Edge> = _edges.filter { it.source == node }

    fun getInEdges(node: Node): List<Edge> = _edges.filter { it.destination == node }

    // Todo this is a quick fix and should be removed later
    fun setAsTree(isTree: Boolean) {
        this.isTree = isTree
    }

    private fun notifyGraphObserver() = graphObserver.forEach {
        it.notifyGraphInvalidated()
    }
}
