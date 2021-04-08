package dev.bandb.graphview.layouts.layered

import android.graphics.*
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.decoration.edge.ArrowDecoration

//TODO throw UnsupportedOperationException("SugiyamaAlgorithm currently not support custom edge renderer!")
class SugiyamaArrowEdgeDecoration @JvmOverloads constructor(private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    strokeWidth = 5f
    color = Color.BLACK
    style = Paint.Style.STROKE
    strokeJoin = Paint.Join.ROUND
    pathEffect = CornerPathEffect(10f)
}) : ArrowDecoration(linePaint) {

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) {
            return
        }
        val adapter = parent.adapter
        if (adapter !is AbstractGraphAdapter) {
            throw RuntimeException(
                    "SugiyamaArrowEdgeDecoration only works with ${AbstractGraphAdapter::class.simpleName}")
        }
        val layout = parent.layoutManager
        if (layout !is SugiyamaLayoutManager) {
            throw RuntimeException(
                    "SugiyamaArrowEdgeDecoration only works with ${SugiyamaLayoutManager::class.simpleName}")
        }

        val graph = adapter.graph
        val edgeData = layout.edgeData
        val nodeData = layout.nodeData
        val path = Path()
        val trianglePaint = Paint(linePaint)
        trianglePaint.style = Paint.Style.FILL

        graph?.edges?.forEach { edge ->
            val source = edge.source
            val (x, y) = source.position
            val destination = edge.destination
            val (x1, y1) = destination.position
            val clippedLine: FloatArray

            if (edgeData.containsKey(edge) && edgeData.getValue(edge).bendPoints.isNotEmpty()) {
                // draw bend points
                val bendPoints = edgeData.getValue(edge).bendPoints
                val size = bendPoints.size

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
                val triangleCentroid = drawTriangle(c, trianglePaint, clippedLine[0], clippedLine[1], clippedLine[2], clippedLine[3])

                path.reset()
                path.moveTo(bendPoints[0], bendPoints[1])
                for (i in 3 until size - 2 step 2) {
                    path.lineTo(bendPoints[i - 1], bendPoints[i])
                }
                path.lineTo(triangleCentroid[0], triangleCentroid[1])
                c.drawPath(path, linePaint)
            } else {
                val startX = x + source.width / 2f
                val startY = y + source.height / 2f
                val stopX = x1 + destination.width / 2f
                val stopY = y1 + destination.height / 2f

                clippedLine = clipLine(startX, startY, stopX, stopY, destination)

                val triangleCentroid = drawTriangle(c, trianglePaint, clippedLine[0], clippedLine[1], clippedLine[2], clippedLine[3])

                c.drawLine(clippedLine[0],
                        clippedLine[1],
                        triangleCentroid[0],
                        triangleCentroid[1], linePaint)
            }
        }
    }
}
