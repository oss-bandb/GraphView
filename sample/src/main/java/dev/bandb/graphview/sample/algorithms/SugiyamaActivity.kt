package dev.bandb.graphview.sample.algorithms

import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.layered.SugiyamaArrowEdgeDecoration
import dev.bandb.graphview.layouts.layered.SugiyamaConfiguration
import dev.bandb.graphview.layouts.layered.SugiyamaLayoutManager
import dev.bandb.graphview.sample.GraphActivity

class SugiyamaActivity : GraphActivity() {

    public override fun setLayoutManager() {
        recyclerView.layoutManager = SugiyamaLayoutManager(this, SugiyamaConfiguration.Builder().build())
    }

    public override fun setEdgeDecoration() {
        recyclerView.addItemDecoration(SugiyamaArrowEdgeDecoration())
    }

    public override fun createGraph(): Graph {
        val graph = Graph()
        val node1 = Node(nodeText)
        val node2 = Node(nodeText)
        val node3 = Node(nodeText)
        val node4 = Node(nodeText)
        val node5 = Node(nodeText)
        val node6 = Node(nodeText)
        val node8 = Node(nodeText)
        val node7 = Node(nodeText)
        val node9 = Node(nodeText)
        val node10 = Node(nodeText)
        val node11 = Node(nodeText)
        val node12 = Node(nodeText)
        val node13 = Node(nodeText)
        val node14 = Node(nodeText)
        val node15 = Node(nodeText)
        val node16 = Node(nodeText)
        val node17 = Node(nodeText)
        val node18 = Node(nodeText)
        val node19 = Node(nodeText)
        val node20 = Node(nodeText)
        val node21 = Node(nodeText)
        val node22 = Node(nodeText)
        val node23 = Node(nodeText)
        graph.addEdge(node1, node13)
        graph.addEdge(node1, node21)
        graph.addEdge(node1, node4)
        graph.addEdge(node1, node3)
        graph.addEdge(node2, node3)
        graph.addEdge(node2, node20)
        graph.addEdge(node3, node4)
        graph.addEdge(node3, node5)
        graph.addEdge(node3, node23)
        graph.addEdge(node4, node6)
        graph.addEdge(node5, node7)
        graph.addEdge(node6, node8)
        graph.addEdge(node6, node16)
        graph.addEdge(node6, node23)
        graph.addEdge(node7, node9)
        graph.addEdge(node8, node10)
        graph.addEdge(node8, node11)
        graph.addEdge(node9, node12)
        graph.addEdge(node10, node13)
        graph.addEdge(node10, node14)
        graph.addEdge(node10, node15)
        graph.addEdge(node11, node15)
        graph.addEdge(node11, node16)
        graph.addEdge(node12, node20)
        graph.addEdge(node13, node17)
        graph.addEdge(node14, node17)
        graph.addEdge(node14, node18)
        graph.addEdge(node16, node18)
        graph.addEdge(node16, node19)
        graph.addEdge(node16, node20)
        graph.addEdge(node18, node21)
        graph.addEdge(node19, node22)
        graph.addEdge(node21, node23)
        graph.addEdge(node22, node23)
        return graph
    }
}