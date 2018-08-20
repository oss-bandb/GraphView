package de.blox.graphview.energy;

import android.graphics.Canvas;
import android.graphics.Paint;

import de.blox.graphview.Vector;
import de.blox.graphview.Edge;
import de.blox.graphview.EdgeRenderer;
import de.blox.graphview.Graph;
import de.blox.graphview.Node;

/**
 *
 */
class EnergyEdgeRenderer implements EdgeRenderer {
    @Override
    public void render(Canvas canvas, Graph graph, Paint paint) {
        for (Edge edge : graph.getEdges()) {
            final Node source = edge.getSource();
            final Vector sourcePosition = source.getPosition();
            final Node destination = edge.getDestination();
            final Vector destinationPosition = destination.getPosition();

            canvas.drawLine((float) sourcePosition.getX() + source.getWidth() / 2,
                    (float) sourcePosition.getY() + source.getHeight() / 2,
                    (float) destinationPosition.getX() + destination.getWidth() / 2,
                    (float) destinationPosition.getY() + destination.getHeight() / 2, paint);
        }
    }
}
