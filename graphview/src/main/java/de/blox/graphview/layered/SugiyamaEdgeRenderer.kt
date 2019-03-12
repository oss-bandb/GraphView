package de.blox.graphview.layered

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import de.blox.graphview.Edge
import de.blox.graphview.Graph
import de.blox.graphview.Node
import de.blox.graphview.edgerenderer.ArrowEdgeRenderer

class SugiyamaEdgeRenderer internal constructor(
    private val nodeData: Map<Node, SugiyamaNodeData>,
    private val edgeData: Map<Edge, SugiyamaEdgeData>
) : ArrowEdgeRenderer() {

    override fun render(canvas: Canvas, graph: Graph, paint: Paint) {
        val trianglePaint = Paint(paint)
        trianglePaint.style = Paint.Style.FILL
        val path = Path()

        graph.edges.forEach { edge ->
            val source = edge.source
            val (x, y) = source.position
            val destination = edge.destination
            val (x1, y1) = destination.position
            val clippedLine: FloatArray

            if (edgeData.containsKey(edge) && !edgeData.getValue(edge).bendPoints.isEmpty()) {
                // draw bend points
                val bendPoints = edgeData.getValue(edge).bendPoints
                val size = bendPoints.size

                path.reset()
                path.moveTo(bendPoints[0], bendPoints[1])

                var i = 3
                while (i < size) {
                    path.lineTo(bendPoints[i - 1], bendPoints[i])
                    i += 2
                }
                canvas.drawPath(path, paint)
                clippedLine = if (nodeData.getValue(source).isReversed) {
                    clipLine(
                        bendPoints[2],
                        bendPoints[3],
                        bendPoints[0],
                        bendPoints[1],
                        destination
                    )
                } else {
                    clipLine(
                        bendPoints[size - 4],
                        bendPoints[size - 3],
                        bendPoints[size - 2],
                        bendPoints[size - 1],
                        destination
                    )
                }
            } else {
                val startX = x + source.width / 2f
                val startY = y + source.height / 2f
                val stopX = x1 + destination.width / 2f
                val stopY = y1 + destination.height / 2f

                clippedLine = clipLine(startX, startY, stopX, stopY, destination)

                canvas.drawLine(
                    clippedLine[0],
                    clippedLine[1],
                    clippedLine[2],
                    clippedLine[3], paint
                )
            }
            drawTriangle(
                canvas,
                trianglePaint,
                clippedLine[0],
                clippedLine[1],
                clippedLine[2],
                clippedLine[3]
            )
        }
    }
}
