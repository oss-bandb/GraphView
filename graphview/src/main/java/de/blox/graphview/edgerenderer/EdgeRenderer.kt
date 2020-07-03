package de.blox.graphview.edgerenderer

import android.graphics.Canvas
import android.graphics.Paint

import de.blox.graphview.Graph

interface EdgeRenderer {
    fun render(canvas: Canvas, graph: Graph, paint: Paint)
}
