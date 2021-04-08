package dev.bandb.graphview.decoration.edge

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter

open class ArrowEdgeDecoration constructor(private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    strokeWidth = 5f    // TODO: move default values res xml
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
                    "GraphLayoutManager only works with ${AbstractGraphAdapter::class.simpleName}")
        }

        val graph = adapter.graph
        val trianglePaint = Paint(linePaint).apply {
            style = Paint.Style.FILL
        }
        graph?.edges?.forEach { (source, destination) ->
            val (x1, y1) = source.position
            val (x2, y2) = destination.position

            val startX = x1 + source.width / 2f
            val startY = y1 + source.height / 2f
            val stopX = x2 + destination.width / 2f
            val stopY = y2 + destination.height / 2f

            val clippedLine = clipLine(startX, startY, stopX, stopY, destination)

            //TODO: modularization
            val triangleCentroid: FloatArray = drawTriangle(c, trianglePaint, clippedLine[0], clippedLine[1], clippedLine[2], clippedLine[3])

            c.drawLine(clippedLine[0],
                    clippedLine[1],
                    triangleCentroid[0],
                    triangleCentroid[1], linePaint)
        }
    }
}
