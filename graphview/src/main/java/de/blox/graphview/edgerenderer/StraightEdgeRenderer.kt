package de.blox.graphview.edgerenderer

import android.graphics.Canvas
import android.graphics.Paint
import de.blox.graphview.Graph

class StraightEdgeRenderer : EdgeRenderer {
    override fun render(canvas: Canvas, graph: Graph, paint: Paint) {
        graph.edges.forEach { (source, destination) ->
            val (x1, y1) = source.position
            val (x2, y2) = destination.position

            canvas.drawLine(
                x1 + source.width / 2f,
                y1 + source.height / 2f,
                x2 + destination.width / 2f,
                y2 + destination.height / 2f, paint
            )
        }
    }
}
