package de.blox.graphview;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 */

public interface Algorithm {
    void run(Graph graph);
    void drawEdges(Canvas canvas, Graph graph, Paint mLinePaint);
    void setEdgeRenderer(EdgeRenderer renderer);
}
