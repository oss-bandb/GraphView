package de.blox.graphview;

import android.graphics.Canvas;
import android.graphics.Paint;

import de.blox.graphview.edgerenderer.EdgeRenderer;

/**
 */

public interface Algorithm {
    void run(Graph graph);
    void drawEdges(Canvas canvas, Graph graph, Paint mLinePaint);
    void setEdgeRenderer(EdgeRenderer renderer);
    Size getGraphSize();
}
