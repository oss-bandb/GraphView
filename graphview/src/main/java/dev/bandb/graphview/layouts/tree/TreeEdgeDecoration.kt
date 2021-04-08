package dev.bandb.graphview.layouts.tree

import android.graphics.*
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter


open class TreeEdgeDecoration constructor(private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    strokeWidth = 5f
    color = Color.BLACK
    style = Paint.Style.STROKE
    strokeJoin = Paint.Join.ROUND
    pathEffect = CornerPathEffect(10f)
}) : RecyclerView.ItemDecoration() {

    private val linePath = Path()
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter
        if (parent.layoutManager == null || adapter == null) {
            return
        }
        if (adapter !is AbstractGraphAdapter) {
            throw RuntimeException(
                    "TreeEdgeDecoration only works with ${AbstractGraphAdapter::class.simpleName}")
        }
        val layout = parent.layoutManager
        if (layout !is BuchheimWalkerLayoutManager) {
            throw RuntimeException(
                    "TreeEdgeDecoration only works with ${BuchheimWalkerLayoutManager::class.simpleName}")
        }

        val configuration = layout.configuration

        val graph = adapter.graph
        if (graph != null && graph.hasNodes()) {
            val nodes = graph.nodes

            for (node in nodes) {
                val children = graph.successorsOf(node)

                for (child in children) {
                    linePath.reset()
                    when (configuration.orientation) {
                        BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM -> {
                            // position at the middle-top of the child
                            linePath.moveTo(child.x + child.width / 2f, child.y)
                            // draws a line from the child's middle-top halfway up to its parent
                            linePath.lineTo(
                                    child.x + child.width / 2f,
                                    child.y - configuration.levelSeparation / 2f
                            )
                            // draws a line from the previous point to the middle of the parents width
                            linePath.lineTo(
                                    node.x + node.width / 2f,
                                    child.y - configuration.levelSeparation / 2f
                            )

                            // position at the middle of the level separation under the parent
                            linePath.moveTo(
                                    node.x + node.width / 2f,
                                    child.y - configuration.levelSeparation / 2f
                            )
                            // draws a line up to the parents middle-bottom
                            linePath.lineTo(
                                    node.x + node.width / 2f,
                                    node.y + node.height
                            )
                        }
                        BuchheimWalkerConfiguration.ORIENTATION_BOTTOM_TOP -> {
                            linePath.moveTo(child.x + child.width / 2f, child.y + child.height)
                            linePath.lineTo(
                                    child.x + child.width / 2f,
                                    child.y + child.height.toFloat() + configuration.levelSeparation / 2f
                            )
                            linePath.lineTo(
                                    node.x + node.width / 2f,
                                    child.y + child.height.toFloat() + configuration.levelSeparation / 2f
                            )

                            linePath.moveTo(
                                    node.x + node.width / 2f,
                                    child.y + child.height.toFloat() + configuration.levelSeparation / 2f
                            )
                            linePath.lineTo(
                                    node.x + node.width / 2f,
                                    node.y + node.height
                            )
                        }
                        BuchheimWalkerConfiguration.ORIENTATION_LEFT_RIGHT -> {
                            linePath.moveTo(child.x, child.y + child.height / 2f)
                            linePath.lineTo(
                                    child.x - configuration.levelSeparation / 2f,
                                    child.y + child.height / 2f
                            )
                            linePath.lineTo(
                                    child.x - configuration.levelSeparation / 2f,
                                    node.y + node.height / 2f
                            )

                            linePath.moveTo(
                                    child.x - configuration.levelSeparation / 2f,
                                    node.y + node.height / 2f
                            )
                            linePath.lineTo(
                                    node.x + node.width,
                                    node.y + node.height / 2f
                            )
                        }
                        BuchheimWalkerConfiguration.ORIENTATION_RIGHT_LEFT -> {
                            linePath.moveTo(child.x + child.width, child.y + child.height / 2f)
                            linePath.lineTo(
                                    child.x + child.width.toFloat() + configuration.levelSeparation / 2f,
                                    child.y + child.height / 2f
                            )
                            linePath.lineTo(
                                    child.x + child.width.toFloat() + configuration.levelSeparation / 2f,
                                    node.y + node.height / 2f
                            )

                            linePath.moveTo(
                                    child.x + child.width.toFloat() + configuration.levelSeparation / 2f,
                                    node.y + node.height / 2f
                            )
                            linePath.lineTo(
                                    node.x + node.width,
                                    node.y + node.height / 2f
                            )
                        }
                    }

                    c.drawPath(linePath, linePaint)
                }
            }
        }
        super.onDraw(c, parent, state)
    }
}
