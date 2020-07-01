package de.blox.graphview.tree

import android.graphics.Canvas
import android.graphics.Paint
import de.blox.graphview.Graph
import de.blox.graphview.Layout
import de.blox.graphview.Node
import de.blox.graphview.edgerenderer.EdgeRenderer
import de.blox.graphview.util.Size
import de.blox.graphview.util.VectorF
import java.util.*
import kotlin.math.max
import kotlin.math.min

class BuchheimWalkerAlgorithm @JvmOverloads constructor(private val configuration: BuchheimWalkerConfiguration = BuchheimWalkerConfiguration.Builder().build()) :
        Layout {
    private val mNodeData: MutableMap<Node, BuchheimWalkerNodeData> = HashMap()
    private var edgeRenderer: EdgeRenderer = TreeEdgeRenderer(configuration)
    private var minNodeHeight = Integer.MAX_VALUE
    private var minNodeWidth = Integer.MAX_VALUE
    private var maxNodeWidth = Integer.MIN_VALUE
    private var maxNodeHeight = Integer.MIN_VALUE

    private val isVertical: Boolean
        get() {
            val orientation = configuration.orientation
            return orientation == BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM || orientation == BuchheimWalkerConfiguration.ORIENTATION_BOTTOM_TOP
        }

    private fun compare(x: Int, y: Int): Int {
        return if (x < y) -1 else if (x == y) 0 else 1
    }

    private fun createNodeData(node: Node): BuchheimWalkerNodeData {
        val nodeData = BuchheimWalkerNodeData().apply {
            ancestor = node
        }
        mNodeData[node] = nodeData

        return nodeData
    }

    private fun getNodeData(node: Node): BuchheimWalkerNodeData {
        return mNodeData.getValue(node)
    }

    private fun firstWalk(graph: Graph, node: Node, depth: Int, number: Int) {
        val nodeData = createNodeData(node)
        nodeData.depth = depth
        nodeData.number = number
        minNodeHeight = min(minNodeHeight, node.height)
        minNodeWidth = min(minNodeWidth, node.width)
        maxNodeWidth = max(maxNodeWidth, node.width)
        maxNodeHeight = max(maxNodeHeight, node.height)

        if (isLeaf(graph, node)) {
            // if the node has no left sibling, prelim(node) should be set to 0, but we don't have to set it
            // here, because it's already initialized with 0
            if (hasLeftSibling(graph, node)) {
                val leftSibling = getLeftSibling(graph, node)
                nodeData.prelim = getPrelim(leftSibling!!) + getSpacing(graph, leftSibling, node)
            }
        } else {
            val leftMost = getLeftMostChild(graph, node)
            val rightMost = getRightMostChild(graph, node)
            var defaultAncestor = leftMost

            var next: Node? = leftMost
            var i = 1
            while (next != null) {
                firstWalk(graph, next, depth + 1, i++)
                defaultAncestor = apportion(graph, next, defaultAncestor)

                next = getRightSibling(graph, next)
            }

            executeShifts(graph, node)

            val isVertical = isVertical
            val midPoint = 0.5 * ((getPrelim(leftMost) + getPrelim(rightMost!!)
                    + (if (isVertical) rightMost.width else rightMost.height).toDouble()) - if (isVertical) node.width else node.height)

            if (hasLeftSibling(graph, node)) {
                val leftSibling = getLeftSibling(graph, node)
                nodeData.prelim = getPrelim(leftSibling!!) + getSpacing(graph, leftSibling, node)
                nodeData.modifier = nodeData.prelim - midPoint
            } else {
                nodeData.prelim = midPoint
            }
        }
    }

    private fun secondWalk(graph: Graph, node: Node, modifier: Double) {
        val nodeData = getNodeData(node)
        val depth = nodeData.depth

        val vertical = isVertical
        node.setPosition(
            VectorF(
                (nodeData.prelim + modifier).toFloat(),
                (depth * (if (vertical) minNodeHeight else minNodeWidth) + depth * configuration.levelSeparation).toFloat()
            )
        )

        graph.successorsOf(node).forEach { w ->
            secondWalk(graph, w, modifier + nodeData.modifier)
        }

    }

    private fun calculateGraphSize(graph: Graph): Size {
        var left = Integer.MAX_VALUE
        var top = Integer.MAX_VALUE
        var right = Integer.MIN_VALUE
        var bottom = Integer.MIN_VALUE
        graph.nodes.forEach { node ->
            left = min(left.toFloat(), node.x).toInt()
            top = min(top.toFloat(), node.y).toInt()
            right = max(right.toFloat(), node.x + node.width).toInt()
            bottom = max(bottom.toFloat(), node.y + node.height).toInt()
        }

        return Size(right - left, bottom - top)
    }


    private fun executeShifts(graph: Graph, node: Node) {
        var shift = 0.0
        var change = 0.0
        var w = getRightMostChild(graph, node)
        while (w != null) {
            val nodeData = getNodeData(w)

            nodeData.prelim = nodeData.prelim + shift
            nodeData.modifier = nodeData.modifier + shift
            change += nodeData.change
            shift += nodeData.shift + change

            w = getLeftSibling(graph, w)
        }
    }

    private fun apportion(graph: Graph, node: Node, defaultAncestor: Node): Node {
        var ancestor = defaultAncestor
        if (hasLeftSibling(graph, node)) {
            val leftSibling = getLeftSibling(graph, node)

            var vip = node
            var vop: Node? = node
            var vim = leftSibling
            var vom: Node? = getLeftMostChild(graph, graph.predecessorsOf(vip)[0])

            var sip = getModifier(vip)
            var sop = getModifier(vop!!)
            var sim = getModifier(vim!!)
            var som = getModifier(vom!!)

            var nextRight = nextRight(graph, vim)
            var nextLeft = nextLeft(graph, vip)

            while (nextRight != null && nextLeft != null) {
                vim = nextRight
                vip = nextLeft
                vom = nextLeft(graph, vom)
                vop = nextRight(graph, vop)

                setAncestor(vop!!, node)

                val shift =
                    getPrelim(vim) + sim - (getPrelim(vip) + sip) + getSpacing(graph, vim, node)
                if (shift > 0) {
                    moveSubtree(ancestor(graph, vim, node, ancestor), node, shift)
                    sip += shift
                    sop += shift
                }

                sim += getModifier(vim)
                sip += getModifier(vip)
                som += getModifier(vom!!)
                sop += getModifier(vop)

                nextRight = nextRight(graph, vim)
                nextLeft = nextLeft(graph, vip)
            }

            if (nextRight != null && nextRight(graph, vop) == null) {
                setThread(vop!!, nextRight)
                setModifier(vop, getModifier(vop) + sim - sop)
            }

            if (nextLeft != null && nextLeft(graph, vom) == null) {
                setThread(vom!!, nextLeft)
                setModifier(vom, getModifier(vom) + sip - som)
                ancestor = node
            }
        }

        return ancestor
    }

    private fun setAncestor(v: Node, ancestor: Node) {
        getNodeData(v).ancestor = ancestor
    }

    private fun setModifier(v: Node, modifier: Double) {
        getNodeData(v).modifier = modifier
    }

    private fun setThread(v: Node, thread: Node?) {
        getNodeData(v).thread = thread
    }

    private fun getPrelim(v: Node): Double {
        return getNodeData(v).prelim
    }

    private fun getModifier(vip: Node): Double {
        return getNodeData(vip).modifier
    }

    private fun moveSubtree(wm: Node, wp: Node, shift: Double) {
        val wpNodeData = getNodeData(wp)
        val wmNodeData = getNodeData(wm)

        val subtrees = wpNodeData.number - wmNodeData.number
        wpNodeData.change = wpNodeData.change - shift / subtrees
        wpNodeData.shift = wpNodeData.shift + shift
        wmNodeData.change = wmNodeData.change + shift / subtrees
        wpNodeData.prelim = wpNodeData.prelim + shift
        wpNodeData.modifier = wpNodeData.modifier + shift
    }

    private fun ancestor(graph: Graph, vim: Node, node: Node, defaultAncestor: Node): Node {
        val vipNodeData = getNodeData(vim)

        return if (graph.predecessorsOf(vipNodeData.ancestor)[0] === graph.predecessorsOf(node)[0]) {
            vipNodeData.ancestor
        } else defaultAncestor
    }

    private fun nextRight(graph: Graph, node: Node?): Node? {
        return if (graph.hasSuccessor(node!!)) {
            getRightMostChild(graph, node)
        } else getNodeData(node).thread
    }

    private fun nextLeft(graph: Graph, node: Node?): Node? {
        return if (graph.hasSuccessor(node!!)) {
            getLeftMostChild(graph, node)
        } else getNodeData(node).thread
    }

    private fun getSpacing(graph: Graph, leftNode: Node?, rightNode: Node): Int {
        var separation = configuration.subtreeSeparation

        if (isSibling(graph, leftNode, rightNode)) {
            separation = configuration.siblingSeparation
        }

        val vertical = isVertical
        return separation + if (vertical) leftNode!!.width else leftNode!!.height
    }

    private fun isSibling(graph: Graph, leftNode: Node?, rightNode: Node): Boolean {
        val leftParent = graph.predecessorsOf(leftNode!!)[0]
        return graph.successorsOf(leftParent).contains(rightNode)
    }

    private fun isLeaf(graph: Graph, node: Node): Boolean {
        return graph.successorsOf(node).isEmpty()
    }

    private fun getLeftSibling(graph: Graph, node: Node): Node? {
        if (!hasLeftSibling(graph, node)) {
            return null
        }

        val parent = graph.predecessorsOf(node)[0]
        val children = graph.successorsOf(parent)
        val nodeIndex = children.indexOf(node)
        return children[nodeIndex - 1]
    }

    private fun hasLeftSibling(graph: Graph, node: Node): Boolean {
        val parents = graph.predecessorsOf(node)
        if (parents.isEmpty()) {
            return false
        }

        val parent = parents[0]
        val nodeIndex = graph.successorsOf(parent).indexOf(node)
        return nodeIndex > 0
    }

    private fun getRightSibling(graph: Graph, node: Node): Node? {
        if (!hasRightSibling(graph, node)) {
            return null
        }

        val parent = graph.predecessorsOf(node)[0]
        val children = graph.successorsOf(parent)
        val nodeIndex = children.indexOf(node)
        return children[nodeIndex + 1]
    }

    private fun hasRightSibling(graph: Graph, node: Node): Boolean {
        val parents = graph.predecessorsOf(node)
        if (parents.isEmpty()) {
            return false
        }
        val parent = parents[0]
        val children = graph.successorsOf(parent)
        val nodeIndex = children.indexOf(node)
        return nodeIndex < children.size - 1
    }

    private fun getLeftMostChild(graph: Graph, node: Node): Node {
        return graph.successorsOf(node)[0]
    }

    private fun getRightMostChild(graph: Graph, node: Node): Node? {
        val children = graph.successorsOf(node)
        return if (children.isEmpty()) {
            null
        } else children[children.size - 1]
    }

    override fun run(graph: Graph, shiftX: Float, shiftY: Float): Size {
        // TODO check for cycles and multiple parents
        mNodeData.clear()

        val firstNode = graph.getNodeAtPosition(0)
        firstWalk(graph, firstNode, 0, 0)

        secondWalk(graph, firstNode, 0.0)

        positionNodes(graph)

        shiftCoordinates(graph, shiftX, shiftY)

        return calculateGraphSize(graph)
    }

    private fun positionNodes(graph: Graph) {
        var globalPadding = 0
        var localPadding = 0
        val offset = getOffset(graph)

        val orientation = configuration.orientation
        val needReverseOrder =
            orientation == BuchheimWalkerConfiguration.ORIENTATION_BOTTOM_TOP || orientation == BuchheimWalkerConfiguration.ORIENTATION_RIGHT_LEFT
        val nodes = sortByLevel(graph, needReverseOrder)

        val firstLevel = getNodeData(nodes[0]).depth
        var localMaxSize = findMaxSize(filterByLevel(nodes, firstLevel))
        var currentLevel = if (needReverseOrder) firstLevel else 0

        nodes.forEach { node ->
            val depth = getNodeData(node).depth
            if (depth != currentLevel) {
                when (configuration.orientation) {
                    BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM, BuchheimWalkerConfiguration.ORIENTATION_LEFT_RIGHT -> globalPadding += localPadding
                    BuchheimWalkerConfiguration.ORIENTATION_BOTTOM_TOP, BuchheimWalkerConfiguration.ORIENTATION_RIGHT_LEFT -> globalPadding -= localPadding
                }
                localPadding = 0
                currentLevel = depth

                localMaxSize = findMaxSize(filterByLevel(nodes, currentLevel))
            }

            val height = node.height
            val width = node.width
            when (configuration.orientation) {
                BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM -> if (height > minNodeHeight) {
                    val diff = height - minNodeHeight
                    localPadding = max(localPadding, diff)
                }
                BuchheimWalkerConfiguration.ORIENTATION_BOTTOM_TOP -> if (height < localMaxSize.height) {
                    val diff = localMaxSize.height - height
                    node.setPosition(node.position.subtract(VectorF(0f, diff.toFloat())))
                    localPadding = max(localPadding, diff)
                }
                BuchheimWalkerConfiguration.ORIENTATION_LEFT_RIGHT -> if (width > minNodeWidth) {
                    val diff = width - minNodeWidth
                    localPadding = max(localPadding, diff)
                }
                BuchheimWalkerConfiguration.ORIENTATION_RIGHT_LEFT -> if (width < localMaxSize.width) {
                    val diff = localMaxSize.width - width
                    node.setPosition(node.position.subtract(VectorF(0f, diff.toFloat())))
                    localPadding = max(localPadding, diff)
                }
            }

            node.setPosition(getPosition(node, globalPadding, offset))
        }
    }

    private fun shiftCoordinates(graph: Graph, shiftX: Float, shiftY: Float) {
        graph.nodes.forEach { node ->
            node.setPosition(VectorF(node.x + shiftX, node.y + shiftY))
        }
    }

    private fun findMaxSize(nodes: List<Node>): Size {
        var width = Integer.MIN_VALUE
        var height = Integer.MIN_VALUE

        nodes.forEach { node ->
            width = max(width, node.width)
            height = max(height, node.height)
        }

        return Size(width, height)
    }

    private fun getOffset(graph: Graph): VectorF {
        var offsetX = java.lang.Float.MAX_VALUE
        var offsetY = java.lang.Float.MAX_VALUE
        when (configuration.orientation) {
            BuchheimWalkerConfiguration.ORIENTATION_BOTTOM_TOP, BuchheimWalkerConfiguration.ORIENTATION_RIGHT_LEFT -> offsetY =
                java.lang.Float.MIN_VALUE
        }

        val orientation = configuration.orientation
        graph.nodes.forEach { node ->
            when (orientation) {
                BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM, BuchheimWalkerConfiguration.ORIENTATION_LEFT_RIGHT -> {
                    offsetX = min(offsetX, node.x)
                    offsetY = min(offsetY, node.y)
                }
                BuchheimWalkerConfiguration.ORIENTATION_BOTTOM_TOP, BuchheimWalkerConfiguration.ORIENTATION_RIGHT_LEFT -> {
                    offsetX = min(offsetX, node.x)
                    offsetY = max(offsetY, node.y)
                }
            }
        }
        return VectorF(offsetX, offsetY)
    }

    private fun getPosition(node: Node, globalPadding: Int, offset: VectorF): VectorF =
            when (configuration.orientation) {
                BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM -> VectorF(
                        node.x - offset.x,
                        node.y + globalPadding
                )
                BuchheimWalkerConfiguration.ORIENTATION_BOTTOM_TOP -> VectorF(
                        node.x - offset.x,
                        offset.y - node.y - globalPadding.toFloat()
                )
                BuchheimWalkerConfiguration.ORIENTATION_LEFT_RIGHT -> VectorF(
                        node.y + globalPadding,
                        node.x - offset.x
                )
                BuchheimWalkerConfiguration.ORIENTATION_RIGHT_LEFT -> VectorF(
                        offset.y - node.y - globalPadding.toFloat(),
                        node.x - offset.x
                )
                else -> {
                    throw IllegalStateException("Unknown Orientation! ${configuration.orientation}")
                }
            }

    private fun sortByLevel(graph: Graph, descending: Boolean): List<Node> {
        val nodes = ArrayList(graph.nodes)
        var comparator = Comparator<Node> { o1, o2 ->
            val data1 = getNodeData(o1)
            val data2 = getNodeData(o2)
            compare(data1.depth, data2.depth)
        }

        if (descending) {
            comparator = Collections.reverseOrder(comparator)
        }

        Collections.sort(nodes, comparator)

        return nodes
    }

    private fun filterByLevel(nodes: List<Node>, level: Int): List<Node> {
        val nodeList = ArrayList(nodes)

        val iterator = nodeList.iterator()
        while (iterator.hasNext()) {
            val node = iterator.next()
            val depth = getNodeData(node).depth
            if (depth != level) {
                iterator.remove()
            }
        }

        return nodeList
    }

    override fun drawEdges(canvas: Canvas, graph: Graph, linePaint: Paint) {
        edgeRenderer.render(canvas, graph, linePaint)
    }

    override fun setEdgeRenderer(renderer: EdgeRenderer) {
        this.edgeRenderer = renderer
    }
}
