package de.blox.graphview

import android.graphics.Canvas
import android.graphics.Paint

import de.blox.graphview.edgerenderer.EdgeRenderer
import de.blox.graphview.util.Size

interface Algorithm {
    val graphSize: Size
    fun run(graph: Graph)
    fun drawEdges(canvas: Canvas, graph: Graph, linePaint: Paint)
    fun setEdgeRenderer(renderer: EdgeRenderer)
}
