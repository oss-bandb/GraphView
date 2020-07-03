package de.blox.graphview.layered

import android.graphics.Canvas
import android.graphics.Paint
import de.blox.graphview.Edge
import de.blox.graphview.Graph
import de.blox.graphview.Layout
import de.blox.graphview.Node
import de.blox.graphview.edgerenderer.EdgeRenderer
import de.blox.graphview.util.Size
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

class SugiyamaAlgorithm @JvmOverloads constructor(private val configuration: SugiyamaConfiguration = SugiyamaConfiguration.Builder().build()) : Layout {
    private val nodeData: MutableMap<Node, SugiyamaNodeData> = HashMap()
    private val edgeData: MutableMap<Edge, SugiyamaEdgeData> = HashMap()
    private val stack: MutableSet<Node> = HashSet()
    private val visited: MutableSet<Node> = HashSet()
    private var layers: MutableList<ArrayList<Node>> = mutableListOf()
    private lateinit var graph: Graph
    private val edgeRenderer: EdgeRenderer = SugiyamaEdgeRenderer(nodeData, edgeData)

    private var nodeCount = 1

    private val dummyText: String
        get() = "Dummy " + nodeCount++

    override fun run(graph: Graph, shiftX: Float, shiftY: Float): Size {
        this.graph = copyGraph(graph)

        reset()

        initSugiyamaData()

        cycleRemoval()

        layerAssignment()

        nodeOrdering()

        coordinateAssignment()

        shiftCoordinates(shiftX, shiftY)

        val graphSize = calculateGraphSize(this.graph)

        denormalize()

        restoreCycle()

        return graphSize
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

    private fun shiftCoordinates(shiftX: Float, shiftY: Float) {
        layers.forEach { arrayList: ArrayList<Node> ->
            arrayList.forEach {
                it.x += shiftX
                it.y += shiftY
            }
        }
    }

    private fun reset() {
        layers.clear()
        stack.clear()
        visited.clear()
        nodeData.clear()
        edgeData.clear()
        nodeCount = 1
    }

    private fun initSugiyamaData() {
        graph.nodes.forEach { node ->
            node.x = 0f
            node.y = 0f
            nodeData[node] = SugiyamaNodeData()
        }
        graph.edges.forEach { edge ->
            edgeData[edge] = SugiyamaEdgeData()
        }
    }

    private fun cycleRemoval() {
        graph.nodes.forEach { node ->
            dfs(node)
        }
    }

    private fun dfs(node: Node) {
        if (visited.contains(node)) {
            return
        }

        visited.add(node)
        stack.add(node)

        graph.getOutEdges(node).forEach { edge ->
            val target = edge.destination
            if (stack.contains(target)) {
                graph.removeEdge(edge)
                graph.addEdge(target, node)
                nodeData.getValue(node).reversed.add(target)
            } else {
                dfs(target)
            }
        }
        stack.remove(node)
    }

    // top sort + add dummy nodes
    private fun layerAssignment() {
        if (graph.nodes.isEmpty()) {
            return
        }

        // build layers
        val copyGraph = copyGraph(graph)
        var roots = getRootNodes(copyGraph)

//        val rootDummy = Node(dummyText)
//        val dummyNodeData = SugiyamaNodeData()
//        dummyNodeData.isDummy = true
//        nodeData[rootDummy] = dummyNodeData
//
//        for(node in roots) {
//            val edge = copyGraph.addEdge(rootDummy, node)
//            edgeData[edge] = SugiyamaEdgeData()
//        }
//
//        roots = getRootNodes(copyGraph)

        while (roots.isNotEmpty()) {
            layers.add(roots)
            copyGraph.removeNodes(*roots.toTypedArray())
            roots = getRootNodes(copyGraph)
        }

        // add dummy's
        for (i in 0 until layers.size - 1) {
            val indexNextLayer = i + 1
            val currentLayer = layers[i]
            val nextLayer = layers[indexNextLayer]

            for (node in currentLayer) {
                val edges = this.graph.edges
                        .filter { (source) -> source == node }
                        .filter { (_, destination) -> abs(nodeData.getValue(destination).layer - nodeData.getValue(node).layer) > 1 }
                        .toMutableList()
                val iterator = edges.iterator()
                while (iterator.hasNext()) {
                    val edge = iterator.next()
                    val dummy = Node(dummyText)
                    val dummyNodeData = SugiyamaNodeData()
                    dummyNodeData.isDummy = true
                    dummyNodeData.layer = indexNextLayer
                    nextLayer.add(dummy)
                    nodeData[dummy] = dummyNodeData
                    dummy.setSize(edge.source.width, 0) // TODO: calc avg layer height
                    val dummyEdge1 = this.graph.addEdge(edge.source, dummy)
                    val dummyEdge2 = this.graph.addEdge(dummy, edge.destination)
                    edgeData[dummyEdge1] = SugiyamaEdgeData()
                    edgeData[dummyEdge2] = SugiyamaEdgeData()
                    this.graph.removeEdge(edge)

                    iterator.remove()
                }
            }
        }
    }

    private fun getRootNodes(graph: Graph): ArrayList<Node> {
        val roots = arrayListOf<Node>()

        graph.nodes.forEach { node ->
            var inDegree = 0
            graph.edges.forEach { (_, destination) ->
                if (destination == node) {
                    inDegree++
                }
            }
            if (inDegree == 0) {
                roots.add(node)
                nodeData.getValue(node).layer = layers.size
            }
        }
        return roots
    }

    private fun copyGraph(graph: Graph): Graph {
        val copy = Graph()
        copy.addNodes(*graph.nodes.toTypedArray())
        copy.addEdges(*graph.edges.toTypedArray())
        return copy
    }

    private fun nodeOrdering() {
        val best = ArrayList(layers)
        (0..23).forEach { i ->
            median(best, i)
            transpose(best)
            if (crossing(best) < crossing(layers)) {
                layers = best
            }
        }
    }

    private fun median(layers: ArrayList<ArrayList<Node>>, currentIteration: Int) {
        if (currentIteration % 2 == 0) {
            for (i in 1 until layers.size) {
                val currentLayer = layers[i]
                val previousLayer = layers[i - 1]
                for (node in currentLayer) {
                    val positions = graph.edges
                            .filter { (source) -> previousLayer.contains(source) }
                            .map { (source) -> previousLayer.indexOf(source) }.toMutableList()
                    positions.sort()
                    val median = positions.size / 2
                    if (positions.isNotEmpty()) {
                        if (positions.size == 1) {
                            nodeData.getValue(node).median = -1
                        } else if (positions.size == 2) {
                            nodeData.getValue(node).median = (positions[0] + positions[1]) / 2
                        } else if (positions.size % 2 == 1) {
                            nodeData.getValue(node).median = positions[median]
                        } else {
                            val left = positions[median - 1] - positions[0]
                            val right = positions[positions.size - 1] - positions[median]
                            if (left + right != 0) {
                                nodeData.getValue(node).median =
                                        (positions[median - 1] * right + positions[median] * left) / (left + right)
                            }
                        }
                    }
                }
                currentLayer.sortWith(Comparator { n1, n2 ->
                    val nodeData1 = nodeData.getValue(n1)
                    val nodeData2 = nodeData.getValue(n2)
                    nodeData1.median - nodeData2.median
                })
            }
        } else {
            for (l in 1 until layers.size) {
                val currentLayer = layers[l]
                val previousLayer = layers[l - 1]
                for (i in currentLayer.size - 1 downTo 1) {
                    val node = currentLayer[i]
                    val positions = graph.edges
                            .filter { (source) -> previousLayer.contains(source) }
                            .map { (source) -> previousLayer.indexOf(source) }.toMutableList()
                    positions.sort()
                    if (positions.isNotEmpty()) {
                        if (positions.size == 1) {
                            nodeData.getValue(node).median = positions[0]
                        } else {
                            nodeData.getValue(node).median = (positions[ceil(positions.size / 2.0).toInt()] + positions[ceil(positions.size / 2.0).toInt() - 1]) / 2
                        }
                    }
                }
                currentLayer.sortWith(Comparator { n1, n2 ->
                    val nodeData1 = nodeData.getValue(n1)
                    val nodeData2 = nodeData.getValue(n2)
                    nodeData1.median - nodeData2.median
                })
            }
        }
    }

    private fun transpose(layers: List<List<Node>>) {
        var improved = true
        while (improved) {
            improved = false
            (0 until layers.size - 1).forEach { l ->
                val northernNodes = layers[l]
                val southernNodes = layers[l + 1]
                (0 until southernNodes.size - 1).forEach { i ->
                    val v = southernNodes[i]
                    val w = southernNodes[i + 1]
                    if (crossing(northernNodes, v, w) > crossing(northernNodes, w, v)) {
                        improved = true
                        exchange(southernNodes, v, w)
                    }
                }
            }
        }
    }

    private fun exchange(nodes: List<Node>, v: Node, w: Node) {
        Collections.swap(nodes, nodes.indexOf(v), nodes.indexOf(w))
    }

    // counts the number of edge crossings if n2 appears to the left of n1 in their layer.
    private fun crossing(northernNodes: List<Node>, n1: Node, n2: Node): Int {
        var crossing = 0

        val parentNodesN1 = graph.edges
                .filter { (_, destination) -> destination == n1 }
                .map { it.source }
                .toList()

        val parentNodesN2 = graph.edges
                .filter { (_, destination) -> destination == n2 }
                .map { it.source }
                .toList()

        parentNodesN2.forEach { pn2 ->
            val indexOfPn2 = northernNodes.indexOf(pn2)
            repeat(
                    parentNodesN1
                            .filter { indexOfPn2 < northernNodes.indexOf(it) }.size
            ) { crossing++ }
        }
        return crossing
    }

    private fun crossing(layers: List<List<Node>>): Int {
        var crossing = 0
        (0 until layers.size - 1).forEach { l ->
            val southernNodes = layers[l]
            val northernNodes = layers[l + 1]
            for (i in 0 until southernNodes.size - 2) {
                val v = southernNodes[i]
                val w = southernNodes[i + 1]
                crossing += crossing(northernNodes, v, w)
            }
        }
        return crossing
    }

    private fun coordinateAssignment() {
        assignX()
        assignY()
    }

    private fun assignX() {
        // each node points to the root of the block.
        val root = ArrayList<MutableMap<Node, Node>>(4)
        // each node points to its aligned neighbor in the layer below.
        val align = ArrayList<MutableMap<Node, Node>>(4)
        val sink = ArrayList<MutableMap<Node, Node>>(4)
        val x = ArrayList<MutableMap<Node, Float>>(4)
        // minimal separation between the roots of different classes.
        val shift = ArrayList<MutableMap<Node, Float>>(4)
        // the width of each block (max width of node in block)
        val blockWidth = ArrayList<MutableMap<Node, Float>>(4)


        (0..3).forEach { i ->
            root.add(HashMap())
            align.add(HashMap())
            sink.add(HashMap())
            shift.add(HashMap())
            x.add(HashMap())
            blockWidth.add(HashMap())
            graph.nodes.forEach { n ->
                root[i][n] = n
                align[i][n] = n
                sink[i][n] = n
                shift[i][n] = java.lang.Float.MAX_VALUE
                x[i][n] = java.lang.Float.MIN_VALUE
                blockWidth[i][n] = 0f
            }
        }
        // calc the layout for down/up and leftToRight/rightToLeft
        (0..1).forEach { downward ->
            val type1Conflicts = markType1Conflicts(downward == 0)
            for (leftToRight in 0..1) {
                val k = 2 * downward + leftToRight

                verticalAlignment(
                        root[k],
                        align[k],
                        type1Conflicts,
                        downward == 0,
                        leftToRight == 0
                )
                computeBlockWidths(root[k], blockWidth[k])
                horizontalCompactation(
                        align[k],
                        root[k],
                        sink[k],
                        shift[k],
                        blockWidth[k],
                        x[k],
                        leftToRight == 0,
                        downward == 0
                )
            }
        }
        balance(x, blockWidth)
    }

    private fun balance(
            x: List<MutableMap<Node, Float>>,
            blockWidth: List<MutableMap<Node, Float>>
    ) {
        val coordinates = HashMap<Node, Float>()

        var minWidth = java.lang.Float.MAX_VALUE
        var smallestWidthLayout = 0
        val min = FloatArray(4)
        val max = FloatArray(4)

        // get the layout with smallest width and set minimum and maximum value
        // for each direction
        (0..3).forEach { i ->
            min[i] = Integer.MAX_VALUE.toFloat()
            max[i] = 0f
            graph.nodes.forEach { v ->
                val bw = 0.5f * blockWidth[i].getValue(v)
                var xp = x[i].getValue(v) - bw
                if (xp < min[i]) {
                    min[i] = xp
                }
                xp = x[i].getValue(v) + bw
                if (xp > max[i]) {
                    max[i] = xp
                }
            }
            val width = max[i] - min[i]
            if (width < minWidth) {
                minWidth = width
                smallestWidthLayout = i
            }
        }

        // align the layouts to the one with smallest width
        (0..3).filter { it != smallestWidthLayout }
                .forEach {
                    // align the left to right layouts to the left border of the
                    // smallest layout
                    if (it == 0 || it == 1) {
                        val diff = min[it] - min[smallestWidthLayout]
                        for (n in x[it].keys) {
                            if (diff > 0) {
                                x[it][n] = x[it].getValue(n) - diff
                            } else {
                                x[it][n] = x[it].getValue(n) + diff
                            }
                        }

                        // align the right to left layouts to the right border of
                        // the smallest layout
                    } else {
                        val diff = max[it] - max[smallestWidthLayout]
                        x[it].keys.forEach { n ->
                            if (diff > 0) {
                                x[it][n] = x[it].getValue(n) - diff
                            } else {
                                x[it][n] = x[it].getValue(n) + diff
                            }
                        }
                    }
                }

        // get the minimum coordinate value
        var minValue = (0..3)
                .flatMap { x[it].values }
                .min()
                ?: java.lang.Float.MAX_VALUE

        // set left border to 0
        if (minValue != 0f) {
            (0..3).forEach { i ->
                x[i].keys.forEach { n ->
                    x[i][n] = x[i].getValue(n) - minValue
                }
            }
        }

        // get the average median of each coordinate
        this.graph.nodes.forEach { n ->
            val values = FloatArray(4)
            (0..3).forEach { i ->
                values[i] = x[i].getValue(n)
            }
            Arrays.sort(values)
            val average = (values[1] + values[2]) / 2
            coordinates[n] = average
        }

        // get the minimum coordinate value
        minValue = coordinates.values.min()
                ?: Integer.MAX_VALUE.toFloat()

        // set left border to 0
        when {
            minValue != 0f -> coordinates.keys.forEach { n ->
                coordinates[n] = coordinates.getValue(n) - minValue
            }
        }

        graph.nodes.forEach { v ->
            v.x = coordinates.getValue(v)
        }
    }

    private fun markType1Conflicts(downward: Boolean): List<List<Boolean>> {
        val type1Conflicts = ArrayList<ArrayList<Boolean>>()

        for (i in graph.nodes.indices) {
            type1Conflicts.add(ArrayList())
            for (l in graph.edges.indices) {
                type1Conflicts[i].add(false)
            }
        }

        if (layers.size >= 4) {
            val upper: Int
            val lower: Int // iteration bounds
            var k1: Int // node position boundaries of closest inner segments
            if (downward) {
                lower = 1
                upper = layers.size - 2
            } else {
                lower = layers.size - 1
                upper = 2
            }

            /*
             * iterate level[2..h-2] in the given direction
             * available levels: 1 to h
             */
            var i = lower
            while (downward && i <= upper || !downward && i >= upper) {
                var k0 = 0
                var firstIndex = 0 // index of first node on layer
                val currentLevel = layers[i]
                val nextLevel = if (downward) layers[i + 1] else layers[i - 1]

                // for all nodes on next level
                (0 until nextLevel.size).forEach { l1 ->
                    val virtualTwin = virtualTwinNode(nextLevel[l1], downward)
                    if (l1 == nextLevel.size - 1 || virtualTwin != null) {
                        k1 = currentLevel.size - 1

                        if (virtualTwin != null) {
                            k1 = pos(virtualTwin)
                        }

                        while (firstIndex <= l1) {
                            val upperNeighbours = getAdjNodes(nextLevel[l1], downward)

                            for (currentNeighbour in upperNeighbours) {
                                /*
                                         * XXX: < 0 in first iteration is still ok for indizes starting
                                         * with 0 because no index can be smaller than 0
                                         */
                                val currentNeighbourIndex = pos(currentNeighbour)
                                if (currentNeighbourIndex < k0 || currentNeighbourIndex > k1) {
                                    type1Conflicts[l1][currentNeighbourIndex] = true
                                }
                            }
                            firstIndex++
                        }
                        k0 = k1
                    }
                }
                i = if (downward) i + 1 else i - 1
            }
        }
        return type1Conflicts
    }

    private fun verticalAlignment(
            root: MutableMap<Node, Node>,
            align: MutableMap<Node, Node>,
            type1Conflicts: List<List<Boolean>>,
            downward: Boolean,
            leftToRight: Boolean
    ) {
        // for all Level
        var i = if (downward) 0 else layers.size - 1
        while (downward && i <= layers.size - 1 || !downward && i >= 0) {
            val currentLevel = layers[i]
            var r = if (leftToRight) -1 else Integer.MAX_VALUE
            // for all nodes on Level i (with direction leftToRight)
            var k = if (leftToRight) 0 else currentLevel.size - 1
            while (leftToRight && k <= currentLevel.size - 1 || !leftToRight && k >= 0) {

                val v = currentLevel[k]
                val adjNodes = getAdjNodes(v, downward)
                if (adjNodes.isNotEmpty()) {
                    // the first median
                    val median = floor((adjNodes.size + 1) / 2.0).toInt()
                    val medianCount = if (adjNodes.size % 2 == 1) 1 else 2

                    // for all median neighbours in direction of H
                    (0 until medianCount).forEach { count ->
                        val m = adjNodes[median + count - 1]
                        val posM = pos(m)

                        if (align[v] == v
                                // if segment (u,v) not marked by type1 conflicts AND ...
                                && !type1Conflicts[pos(v)][posM]
                                && (leftToRight && r < posM || !leftToRight && r > posM)
                        ) {
                            align[m] = v
                            root[v] = root.getValue(m)
                            align[v] = root.getValue(v)
                            r = posM
                        }
                    }
                }
                k = if (leftToRight) k + 1 else k - 1
            }
            i = if (downward) i + 1 else i - 1
        }
    }

    private fun computeBlockWidths(
            root: MutableMap<Node, Node>,
            blockWidth: MutableMap<Node, Float>
    ) {
        graph.nodes.forEach { v ->
            val r = root.getValue(v)
            blockWidth[r] = max(blockWidth.getValue(r), v.width.toFloat())
        }
    }

    private fun horizontalCompactation(
            align: MutableMap<Node, Node>,
            root: MutableMap<Node, Node>,
            sink: MutableMap<Node, Node>,
            shift: MutableMap<Node, Float>,
            blockWidth: MutableMap<Node, Float>,
            x: MutableMap<Node, Float>,
            leftToRight: Boolean,
            downward: Boolean
    ) {

        // calculate class relative coordinates for all roots
        var i = if (downward) 0 else layers.size - 1
        while (downward && i <= layers.size - 1 || !downward && i >= 0) {
            val currentLevel = layers[i]

            var j = if (leftToRight) 0 else currentLevel.size - 1
            while (leftToRight && j <= currentLevel.size - 1 || !leftToRight && j >= 0) {
                val v = currentLevel[j]
                if (root[v] == v) {
                    placeBlock(v, sink, shift, x, align, blockWidth, root, leftToRight)
                }
                j = if (leftToRight) j + 1 else j - 1
            }
            i = if (downward) i + 1 else i - 1
        }
        var d = 0f
        i = if (downward) 0 else layers.size - 1
        while (downward && i <= layers.size - 1 || !downward && i >= 0) {
            val currentLevel = layers[i]

            val v = currentLevel[if (leftToRight) 0 else currentLevel.size - 1]

            if (v == sink[root[v]]) {
                val oldShift = shift.getValue(v)
                if (oldShift < java.lang.Float.MAX_VALUE) {
                    shift[v] = oldShift + d
                    d += oldShift
                } else {
                    shift[v] = 0f
                }
            }
            i = if (downward) i + 1 else i - 1
        }

        // apply root coordinates for all aligned nodes
        // (place block did this only for the roots)+
        graph.nodes.forEach { v ->
            x[v] = x.getValue(root.getValue(v))

            val shiftVal = shift.getValue(sink.getValue(root.getValue(v)))
            if (shiftVal < java.lang.Float.MAX_VALUE) {
                x[v] = x.getValue(v) + shiftVal  // apply shift for each class
            }
        }
    }

    private fun placeBlock(
            v: Node,
            sink: MutableMap<Node, Node>,
            shift: MutableMap<Node, Float>,
            x: MutableMap<Node, Float>,
            align: MutableMap<Node, Node>,
            blockWidth: MutableMap<Node, Float>,
            root: MutableMap<Node, Node>,
            leftToRight: Boolean
    ) {
        if (x[v] == java.lang.Float.MIN_VALUE) {
            x[v] = 0f
            var w = v
            do {
                // if not first node on layer
                if (leftToRight && pos(w) > 0 || !leftToRight && pos(w) < layers[getLayerIndex(w)].size - 1) {
                    val pred = pred(w, leftToRight)
                    val u = root.getValue(pred!!)

                    placeBlock(u, sink, shift, x, align, blockWidth, root, leftToRight)

                    if (sink[v] == v) {
                        sink[v] = sink.getValue(u)
                    }

                    if (sink[v] != sink[u]) {
                        if (leftToRight) {
                            shift[sink.getValue(u)] = min(shift.getValue(sink.getValue(u)), x.getValue(v) - x.getValue(u) - configuration.nodeSeparation.toFloat() - 0.5f * (blockWidth.getValue(u) + blockWidth.getValue(v)))
                        } else {
                            shift[sink.getValue(u)] = max(shift.getValue(sink.getValue(u)), x.getValue(v) - x.getValue(u) + configuration.nodeSeparation.toFloat() + 0.5f * (blockWidth.getValue(u) + blockWidth.getValue(v)))
                        }
                    } else {
                        if (leftToRight) {
                            x[v] = max(x.getValue(v), x.getValue(u) + configuration.nodeSeparation.toFloat() + 0.5f * (blockWidth.getValue(u) + blockWidth.getValue(v)))
                        } else {
                            x[v] = min(x.getValue(v), x.getValue(u) - configuration.nodeSeparation.toFloat() - 0.5f * (blockWidth.getValue(u) + blockWidth.getValue(v)))
                        }
                    }
                }
                w = align.getValue(w)
            } while (w != v)
        }
    }

    // predecessor
    private fun pred(v: Node, leftToRight: Boolean): Node? {
        val pos = pos(v)
        val rank = getLayerIndex(v)

        val level = layers[rank]
        return if (leftToRight && pos != 0 || !leftToRight && pos != level.size - 1) {
            level[if (leftToRight) pos - 1 else pos + 1]
        } else null
    }


    private fun virtualTwinNode(node: Node, downward: Boolean): Node? {
        if (!isLongEdgeDummy(node)) {
            return null
        }
        val adjNodes = getAdjNodes(node, downward)

        return if (adjNodes.isEmpty()) {
            null
        } else adjNodes[0]
    }


    private fun getAdjNodes(node: Node, downward: Boolean): List<Node> {
        return if (downward) graph.predecessorsOf(node) else graph.successorsOf(node)
    }

    // get node index in layer
    private fun pos(node: Node): Int {
        layers.forEach { l ->
            l.forEach { n ->
                if (node == n) {
                    return l.indexOf(node)
                }
            }
        }
        return -1 // or exception?
    }

    private fun getLayerIndex(node: Node): Int {
        layers.indices.forEach { l ->
            layers[l].forEach { n ->
                if (node == n) {
                    return l
                }
            }
        }
        return -1 // or exception?
    }

    private fun isLongEdgeDummy(v: Node): Boolean {
        val successors = graph.successorsOf(v)
        return nodeData.getValue(v).isDummy && successors.size == 1 && nodeData.getValue(successors[0]).isDummy
    }

    private fun assignY() {
        // compute y-coordinates
        val k = layers.size

        // compute height of each layer

        val height = FloatArray(graph.nodes.size)
        height.fill(0f)

        (0 until k).forEach { i ->
            val level = layers[i]
            for (j in level.indices) {
                val node = level[j]
                val h = if (nodeData.getValue(node).isDummy) 0f else node.height.toFloat()
                if (h > height[i])
                    height[i] = h
            }
        }

        // assign y-coordinates
        var yPos = 0f

        var i = 0
        while (true) {
            val level = layers[i]
            for (j in level.indices)
                level[j].y = yPos

            if (i == k - 1)
                break

            yPos += (configuration.levelSeparation + 0.5 * (height[i] + height[i + 1])).toFloat()
            ++i
        }
    }

    private fun denormalize() {
        // remove dummy's
        for (i in 1 until layers.size - 1) {
            val iterator = layers[i].iterator()
            while (iterator.hasNext()) {
                val current = iterator.next()
                if (nodeData.getValue(current).isDummy) {

                    val predecessor = graph.predecessorsOf(current)[0]
                    val successor = graph.successorsOf(current)[0]

                    val bendPoints =
                            edgeData.getValue(graph.getEdgeBetween(predecessor, current)!!).bendPoints

                    if (bendPoints.isEmpty() || !bendPoints.contains(current.x + predecessor.width / 2f)) {
                        bendPoints.add(predecessor.x + predecessor.width / 2f)
                        bendPoints.add(predecessor.y + predecessor.height / 2f)

                        bendPoints.add(current.x + predecessor.width / 2f)
                        bendPoints.add(current.y)
                    }

                    if (!nodeData.getValue(predecessor).isDummy) {
                        bendPoints.add(current.x + predecessor.width / 2f)
                    } else {
                        bendPoints.add(current.x)
                    }
                    bendPoints.add(current.y)

                    if (nodeData.getValue(successor).isDummy) {
                        bendPoints.add(successor.x + predecessor.width / 2f)
                    } else {
                        bendPoints.add(successor.x + successor.width / 2f)
                    }
                    bendPoints.add(successor.y + successor.height / 2f)

                    graph.removeEdge(predecessor, current)
                    graph.removeEdge(current, successor)
                    val edge = graph.addEdge(predecessor, successor)
                    val sugiyamaEdgeData = SugiyamaEdgeData()
                    sugiyamaEdgeData.bendPoints = bendPoints
                    edgeData[edge] = sugiyamaEdgeData

                    iterator.remove()
                    graph.removeNode(current)
                }
            }
        }
    }

    private fun restoreCycle() {
        graph.nodes.forEach { n ->
            if (nodeData.getValue(n).isReversed) {
                nodeData.getValue(n).reversed.forEach { target ->
                    val bendPoints = edgeData.getValue(graph.getEdgeBetween(target, n)!!).bendPoints
                    graph.removeEdge(target, n)
                    val edge = graph.addEdge(n, target)
                    val edgeData = SugiyamaEdgeData()
                    edgeData.bendPoints = bendPoints
                    this.edgeData[edge] = edgeData
                }
            }
        }
    }

    override fun drawEdges(canvas: Canvas, graph: Graph, linePaint: Paint) {
        edgeRenderer.render(canvas, this.graph, linePaint)
    }

    override fun setEdgeRenderer(renderer: EdgeRenderer) {
        throw UnsupportedOperationException("SugiyamaAlgorithm currently not support custom edge renderer!")
    }
}
