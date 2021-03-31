package dev.bandb.graphview.sample.algorithms

import dev.bandb.graphview.Graph
import dev.bandb.graphview.Node
import dev.bandb.graphview.edgerenderer.ArrowEdgeDecoration
import dev.bandb.graphview.energy.FruchtermanReingoldLayoutManager
import dev.bandb.graphview.sample.GraphActivity

class FruchtermanReingoldActivity : GraphActivity() {

    public override fun setLayoutManager() {
        graphView.layoutManager = FruchtermanReingoldLayoutManager(this, 1000)
    }

    public override fun setEdgeDecoration() {
        graphView.addItemDecoration(ArrowEdgeDecoration())
    }

    public override fun createGraph(): Graph {
        val graph = Graph()
        val a = Node(nodeText)
        val b = Node(nodeText)
        val c = Node(nodeText)
        val d = Node(nodeText)
        val e = Node(nodeText)
        val f = Node(nodeText)
        val g = Node(nodeText)
        val h = Node(nodeText)
        graph.addEdge(a, b)
        graph.addEdge(a, c)
        graph.addEdge(a, d)
        graph.addEdge(c, e)
        graph.addEdge(d, f)
        graph.addEdge(f, c)
        graph.addEdge(g, c)
        graph.addEdge(h, g)
        return graph
    }
}