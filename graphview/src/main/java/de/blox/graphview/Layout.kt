package de.blox.graphview

import android.graphics.Canvas
import android.graphics.Paint

import de.blox.graphview.edgerenderer.EdgeRenderer
import de.blox.graphview.util.Size

interface Layout {
    /**
     * Executes the algorithm.
     * @param shiftY Shifts the y-coordinate origin
     * @param shiftX Shifts the x-coordinate origin
     * @return The size of the graph
     */
    fun run(graph: Graph, shiftX: Float, shiftY: Float): Size
    fun drawEdges(canvas: Canvas, graph: Graph, linePaint: Paint)
    fun setEdgeRenderer(renderer: EdgeRenderer)
}
