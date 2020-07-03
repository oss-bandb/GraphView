package de.blox.graphview.tree

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import de.blox.graphview.Graph
import de.blox.graphview.edgerenderer.EdgeRenderer
import de.blox.graphview.tree.BuchheimWalkerConfiguration.Companion.ORIENTATION_BOTTOM_TOP
import de.blox.graphview.tree.BuchheimWalkerConfiguration.Companion.ORIENTATION_LEFT_RIGHT
import de.blox.graphview.tree.BuchheimWalkerConfiguration.Companion.ORIENTATION_RIGHT_LEFT
import de.blox.graphview.tree.BuchheimWalkerConfiguration.Companion.ORIENTATION_TOP_BOTTOM

class TreeEdgeRenderer(private val configuration: BuchheimWalkerConfiguration) : EdgeRenderer {
    private val linePath = Path()

    override fun render(canvas: Canvas, graph: Graph, paint: Paint) {
        val nodes = graph.nodes

        for (node in nodes) {
            val children = graph.successorsOf(node)

            for (child in children) {
                linePath.reset()
                when (configuration.orientation) {
                    ORIENTATION_TOP_BOTTOM -> {
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
                    ORIENTATION_BOTTOM_TOP -> {
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
                    ORIENTATION_LEFT_RIGHT -> {
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
                    ORIENTATION_RIGHT_LEFT -> {
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

                canvas.drawPath(linePath, paint)
            }
        }
    }
}
