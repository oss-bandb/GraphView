package de.blox.graphview.edgerenderer;

import android.graphics.Canvas;
import android.graphics.Paint;

import de.blox.graphview.Edge;
import de.blox.graphview.Graph;
import de.blox.graphview.Node;
import de.blox.graphview.Vector;

/**
 *
 */
public class StraightEdgeRenderer implements EdgeRenderer {
    @Override
    public void render(Canvas canvas, Graph graph, Paint paint) {
        for (Edge edge : graph.getEdges()) {
            final Node source = edge.getSource();
            final Vector sourcePosition = source.getPosition();
            final Node destination = edge.getDestination();
            final Vector destinationPosition = destination.getPosition();

            canvas.drawLine((float) sourcePosition.getX() + source.getWidth() / 2f,
                    (float) sourcePosition.getY() + source.getHeight() / 2f,
                    (float) destinationPosition.getX() + destination.getWidth() / 2f,
                    (float) destinationPosition.getY() + destination.getHeight() / 2f, paint);
        }
    }
}
