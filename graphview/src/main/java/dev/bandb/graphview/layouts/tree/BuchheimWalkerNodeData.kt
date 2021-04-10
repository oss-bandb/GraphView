package dev.bandb.graphview.layouts.tree

import dev.bandb.graphview.graph.Node

internal class BuchheimWalkerNodeData {
    lateinit var ancestor: Node
    var thread: Node? = null
    var number: Int = 0
    var depth: Int = 0
    var prelim: Double = 0.toDouble()
    var modifier: Double = 0.toDouble()
    var shift: Double = 0.toDouble()
    var change: Double = 0.toDouble()
}
