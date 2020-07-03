package de.blox.graphview.energy

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import de.blox.graphview.Edge
import de.blox.graphview.Graph
import de.blox.graphview.Layout
import de.blox.graphview.Node
import de.blox.graphview.edgerenderer.ArrowEdgeRenderer
import de.blox.graphview.edgerenderer.EdgeRenderer
import de.blox.graphview.util.Size
import de.blox.graphview.util.VectorF
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class FruchtermanReingoldAlgorithm @JvmOverloads constructor(private val iterations: Int = DEFAULT_ITERATIONS) :
        Layout {
    private var edgeRenderer: EdgeRenderer = ArrowEdgeRenderer()
    private val disps: MutableMap<Node, VectorF> = HashMap()
    private val rand = Random(SEED)
    private var width: Int = 0
    private var height: Int = 0
    private var k: Float = 0.toFloat()
    private var t: Float = 0.toFloat()
    private var attraction_k: Float = 0.toFloat()
    private var repulsion_k: Float = 0.toFloat()

    private fun randomize(nodes: List<Node>) {
        nodes.forEach { node ->
            // create meta data for each node
            disps[node] = VectorF()
            node.setPosition(
                    randInt(rand, 0, width / 2).toFloat(),
                    randInt(rand, 0, height / 2).toFloat()
            )
        }
    }

    private fun cool(currentIteration: Int) {
        t *= (1.0f - currentIteration / iterations.toFloat())
    }

    private fun limitMaximumDisplacement(nodes: List<Node>) {
        nodes.forEach {
            val dispLength = max(EPSILON, getDisp(it).length().toDouble()).toFloat()
            it.setPosition(
                    it.position.add(
                            getDisp(it).divide(dispLength).multiply(
                                    min(
                                            dispLength,
                                            t
                                    )
                            )
                    )
            )
        }
    }

    private fun calculateAttraction(edges: List<Edge>) {
        edges.forEach { (v, u) ->
            val delta = v.position.subtract(u.position)
            val deltaLength = max(EPSILON, delta.length().toDouble()).toFloat()
            setDisp(
                    v,
                    getDisp(v).subtract(delta.divide(deltaLength).multiply(forceAttraction(deltaLength)))
            )
            setDisp(
                    u,
                    getDisp(u).add(delta.divide(deltaLength).multiply(forceAttraction(deltaLength)))
            )
        }
    }

    private fun calculateRepulsion(nodes: List<Node>) {
        nodes.forEach { v ->
            nodes.forEach { u ->
                if (u != v) {
                    val delta = v.position.subtract(u.position)
                    val deltaLength = max(EPSILON, delta.length().toDouble()).toFloat()
                    setDisp(
                            v,
                            getDisp(v).add(delta.divide(deltaLength).multiply(forceRepulsion(deltaLength)))
                    )
                }
            }
        }
    }

    private fun forceAttraction(x: Float): Float {
        return x * x / attraction_k
    }

    private fun forceRepulsion(x: Float): Float {
        return repulsion_k * repulsion_k / x
    }

    private fun getDisp(node: Node): VectorF {
        return disps.getValue(node)
    }

    private fun setDisp(node: Node, disp: VectorF) {
        disps[node] = disp
    }

    override fun run(graph: Graph, shiftX: Float, shiftY: Float): Size {
        val size = findBiggestSize(graph) * graph.nodeCount
        width = size
        height = size

        val nodes = graph.nodes
        val edges = graph.edges

        t = (0.1 * sqrt((width / 2f * height / 2f).toDouble())).toFloat()
        k = (0.75 * sqrt((width * height / nodes.size.toFloat()).toDouble())).toFloat()

        attraction_k = 0.75f * k
        repulsion_k = 0.75f * k

        randomize(nodes)

        (0 until iterations).forEach { i ->
            calculateRepulsion(nodes)

            calculateAttraction(edges)

            limitMaximumDisplacement(nodes)

            cool(i)

            if (done()) {
                return@forEach
            }
        }

        positionNodes(graph)

        shiftCoordinates(graph, shiftX, shiftY)

        return calculateGraphSize(graph)
    }

    private fun shiftCoordinates(graph: Graph, shiftX: Float, shiftY: Float) {
        graph.nodes.forEach { node ->
            node.setPosition(VectorF(node.x + shiftX, node.y + shiftY))
        }
    }

    private fun positionNodes(graph: Graph) {
        val (x, y) = getOffset(graph)
        val nodesVisited = ArrayList<Node>()
        val nodeClusters = ArrayList<NodeCluster>()
        graph.nodes.forEach { node ->
            node.setPosition(VectorF(node.x - x, node.y - y))
        }

        graph.nodes.forEach { node ->
            if (nodesVisited.contains(node)) {
                return@forEach
            }

            nodesVisited.add(node)
            var cluster = findClusterOf(nodeClusters, node)
            if (cluster == null) {
                cluster = NodeCluster()
                cluster.add(node)
                nodeClusters.add(cluster)
            }

            followEdges(graph, cluster, node, nodesVisited)
        }

        positionCluster(nodeClusters)
    }

    private fun positionCluster(nodeClusters: MutableList<NodeCluster>) {
        combineSingleNodeCluster(nodeClusters)

        var cluster = nodeClusters[0]
        // move first cluster to 0,0
        cluster.offset(-cluster.rect.left, -cluster.rect.top)

        for (i in 1 until nodeClusters.size) {
            val nextCluster = nodeClusters[i]
            val xDiff = nextCluster.rect.left - cluster.rect.right - CLUSTER_PADDING.toFloat()
            val yDiff = nextCluster.rect.top - cluster.rect.top
            nextCluster.offset(-xDiff, -yDiff)
            cluster = nextCluster
        }
    }

    private fun combineSingleNodeCluster(nodeClusters: MutableList<NodeCluster>) {
        var firstSingleNodeCluster: NodeCluster? = null
        val iterator = nodeClusters.iterator()
        while (iterator.hasNext()) {
            val cluster = iterator.next()
            if (cluster.size() == 1) {
                if (firstSingleNodeCluster == null) {
                    firstSingleNodeCluster = cluster
                    continue
                }

                firstSingleNodeCluster.concat(cluster)
                iterator.remove()
            }
        }
    }

    private fun followEdges(
            graph: Graph,
            cluster: NodeCluster,
            node: Node,
            nodesVisited: MutableList<Node>
    ) {
        graph.successorsOf(node).forEach { successor ->
            if (nodesVisited.contains(successor)) {
                return@forEach
            }

            nodesVisited.add(successor)
            cluster.add(successor)

            followEdges(graph, cluster, successor, nodesVisited)
        }

        graph.predecessorsOf(node).forEach { predecessor ->
            if (nodesVisited.contains(predecessor)) {
                return@forEach
            }

            nodesVisited.add(predecessor)
            cluster.add(predecessor)

            followEdges(graph, cluster, predecessor, nodesVisited)
        }
    }

    private fun findClusterOf(clusters: List<NodeCluster>, node: Node): NodeCluster? {
        return clusters.firstOrNull { it.contains(node) }
    }

    private fun findBiggestSize(graph: Graph): Int {
        return graph.nodes
                .map { max(it.height, it.width) }
                .max()
                ?: 0
    }

    private fun getOffset(graph: Graph): VectorF {
        var offsetX = java.lang.Float.MAX_VALUE
        var offsetY = java.lang.Float.MAX_VALUE
        graph.nodes.forEach { node ->
            offsetX = min(offsetX, node.x)
            offsetY = min(offsetY, node.y)
        }
        return VectorF(offsetX, offsetY)
    }

    private fun done(): Boolean {
        return t < 1.0 / max(height, width)
    }

    override fun drawEdges(canvas: Canvas, graph: Graph, linePaint: Paint) {
        edgeRenderer.render(canvas, graph, linePaint)
    }

    override fun setEdgeRenderer(renderer: EdgeRenderer) {
        this.edgeRenderer = renderer
    }

    private fun calculateGraphSize(graph: Graph): Size {

        var left = Integer.MAX_VALUE
        var top = Integer.MAX_VALUE
        var right = Integer.MIN_VALUE
        var bottom = Integer.MIN_VALUE
        for (node in graph.nodes) {
            left = min(left.toFloat(), node.x).toInt()
            top = min(top.toFloat(), node.y).toInt()
            right = max(right.toFloat(), node.x + node.width).toInt()
            bottom = max(bottom.toFloat(), node.y + node.height).toInt()
        }

        return Size(right - left, bottom - top)
    }

    private class NodeCluster {
        val nodes = arrayListOf<Node>()
        var rect: RectF = RectF()

        fun add(node: Node) {
            nodes.add(node)

            if (nodes.size == 1) {
                rect.apply {
                    left = node.x
                    top = node.y
                    right = node.x + node.width
                    bottom = node.y + node.height
                }
            } else {
                rect.apply {
                    left = min(left, node.x)
                    top = min(top, node.y)
                    right = max(right, node.x + node.width)
                    bottom = max(bottom, node.y + node.height)
                }
            }
        }

        operator fun contains(node: Node): Boolean {
            return nodes.contains(node)
        }

        fun size(): Int {
            return nodes.size
        }

        fun concat(cluster: NodeCluster) {
            cluster.nodes.forEach { node ->
                node.setPosition(VectorF(rect.right + CLUSTER_PADDING, rect.top))
                add(node)
            }
        }

        fun offset(xDiff: Float, yDiff: Float) {
            nodes.forEach { node ->
                node.setPosition(node.position.add(xDiff, yDiff))
            }

            rect.offset(xDiff, yDiff)
        }
    }

    companion object {
        const val DEFAULT_ITERATIONS = 1000
        const val CLUSTER_PADDING = 100
        private const val EPSILON = 0.0001
        private const val SEED = 401678L

        private fun randInt(rand: Random, min: Int, max: Int): Int {
            // nextInt is normally exclusive of the top value,
            // so add 1 to make it inclusive
            return rand.nextInt(max - min + 1) + min
        }
    }
}
