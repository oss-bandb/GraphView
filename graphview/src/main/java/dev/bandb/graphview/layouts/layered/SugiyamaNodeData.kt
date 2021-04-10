package dev.bandb.graphview.layouts.layered

import dev.bandb.graphview.graph.Node

internal class SugiyamaNodeData {
    val reversed = mutableSetOf<Node>()
    var isDummy = false
    var median = -1
    var layer = -1

    val isReversed: Boolean
        get() = reversed.isNotEmpty()

    override fun toString(): String {
        return "SugiyamaNodeData{" +
                ", reversed=" + reversed +
                ", dummy=" + isDummy +
                ", median=" + median +
                ", layer=" + layer +
                '}'.toString()
    }
}
