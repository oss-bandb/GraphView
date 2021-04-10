package dev.bandb.graphview.decoration.edge

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter

open class StraightEdgeDecoration constructor(private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    strokeWidth = 5f
    color = Color.BLACK
    style = Paint.Style.STROKE
    strokeJoin = Paint.Join.ROUND
    pathEffect = CornerPathEffect(10f)
}) : RecyclerView.ItemDecoration() {
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

        graph?.edges?.forEach { (source, destination) ->
            val (x1, y1) = source.position
            val (x2, y2) = destination.position

            c.drawLine(
                    x1 + source.width / 2f,
                    y1 + source.height / 2f,
                    x2 + destination.width / 2f,
                    y2 + destination.height / 2f, linePaint
            )
        }
        super.onDraw(c, parent, state)
    }
}
