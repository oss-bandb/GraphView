package de.blox.graphview.edgerenderer;

import android.graphics.Canvas;
import android.graphics.Paint;

import de.blox.graphview.Graph;

/**
 *
 */
public interface EdgeRenderer {
    void render(Canvas canvas, Graph graph, Paint paint);
}
