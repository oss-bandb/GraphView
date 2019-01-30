package de.blox.graphview.layered;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.List;
import java.util.Map;

import de.blox.graphview.Edge;
import de.blox.graphview.Graph;
import de.blox.graphview.Node;
import de.blox.graphview.Vector;
import de.blox.graphview.edgerenderer.ArrowEdgeRenderer;

public class SugiyamaEdgeRenderer extends ArrowEdgeRenderer {

    private Map<Edge, SugiyamaEdgeData> edgeData;

    SugiyamaEdgeRenderer(Map<Edge, SugiyamaEdgeData> edgeData) {
        this.edgeData = edgeData;
    }

    @Override
    public void render(Canvas canvas, Graph graph, Paint paint) {
        Paint trianglePaint = new Paint(paint);
        trianglePaint.setStyle(Paint.Style.FILL);
        Path path = new Path();

        for (Edge edge : graph.getEdges()) {
            final Node source = edge.getSource();
            final Vector sourcePosition = source.getPosition();
            final Node destination = edge.getDestination();
            final Vector destinationPosition = destination.getPosition();
            float[] clippedLine;

            if (edgeData.get(edge).bendPoints.isEmpty()) {
                final float startX = sourcePosition.getX() + source.getWidth() / 2f;
                final float startY = sourcePosition.getY() + source.getHeight() / 2f;
                float stopX = destinationPosition.getX() + destination.getWidth() / 2f;
                float stopY = destinationPosition.getY() + destination.getHeight() / 2f;

                clippedLine = clipLine(startX, startY, stopX, stopY, destination);

                canvas.drawLine(clippedLine[0],
                        clippedLine[1],
                        clippedLine[2],
                        clippedLine[3], paint);
            } else {
                // draw bend points
                final List<Float> bendPoints = edgeData.get(edge).bendPoints;
                final int size = bendPoints.size();

                path.reset();
                path.moveTo(bendPoints.get(0), bendPoints.get(1));

                for (int i = 3; i < size; i = i + 2) {
                    path.lineTo(bendPoints.get(i - 1), bendPoints.get(i));
                }
                canvas.drawPath(path, paint);
                clippedLine = clipLine(bendPoints.get(size - 4), bendPoints.get(size - 3), bendPoints.get(size - 2), bendPoints.get(size - 1), destination);
            }
            drawTriangle(canvas, trianglePaint, clippedLine[0], clippedLine[1], clippedLine[2], clippedLine[3]);
        }
    }
}
