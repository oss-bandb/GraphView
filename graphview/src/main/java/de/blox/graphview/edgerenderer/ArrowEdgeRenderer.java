package de.blox.graphview.edgerenderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import de.blox.graphview.Edge;
import de.blox.graphview.Graph;
import de.blox.graphview.Node;
import de.blox.graphview.Vector;

public class ArrowEdgeRenderer implements EdgeRenderer {
    private static final float ARROW_DEGREES = 0.5f;
    private static final float ARROW_LENGTH = 50f;

    private Path trianglePath = new Path();

    @Override
    public void render(Canvas canvas, Graph graph, Paint paint) {
        Paint trianglePaint = new Paint(paint);
        trianglePaint.setStyle(Paint.Style.FILL);

        for (Edge edge : graph.getEdges()) {
            final Node source = edge.getSource();
            final Vector sourcePosition = source.getPosition();
            final Node destination = edge.getDestination();
            final Vector destinationPosition = destination.getPosition();

            final float startX = sourcePosition.getX() + source.getWidth() / 2f;
            final float startY = sourcePosition.getY() + source.getHeight() / 2f;
            float stopX = destinationPosition.getX() + destination.getWidth() / 2f;
            float stopY = destinationPosition.getY() + destination.getHeight() / 2f;

            float[] clippedLine = clipLine(startX, startY, stopX, stopY, destination);

            float[] triangleCentroid = drawTriangle(canvas, trianglePaint, clippedLine[0], clippedLine[1], clippedLine[2], clippedLine[3]);

            canvas.drawLine(clippedLine[0],
                    clippedLine[1],
                    triangleCentroid[0],
                    triangleCentroid[1], paint);

        }
    }

    /**
     * Clips a line to stop just before the bounds of the destination node.
     *
     * @param startX
     * @param startY
     * @param stopX
     * @param stopY
     * @param destination
     * @return
     */
    protected float[] clipLine(float startX, float startY, float stopX, float stopY, Node destination) {
        final float[] resultLine = new float[4];
        resultLine[0] = startX;
        resultLine[1] = startY;

        final float slope = (startY - stopY) / (startX - stopX);
        final float halfHeight = destination.getHeight() / 2f;
        final float halfWidth = destination.getWidth() / 2f;
        final float halfSlopeWidth = slope * halfWidth;
        final float halfSlopeHeight = halfHeight / slope;

        if (-halfHeight <= halfSlopeWidth && halfSlopeWidth <= halfHeight) {
            // line intersects with ...
            if (destination.getX() > startX) {
                // left edge
                resultLine[2] = stopX - halfWidth;
                resultLine[3] = stopY - halfSlopeWidth;
            } else if (destination.getX() < startX) {
                // right edge
                resultLine[2] = stopX + halfWidth;
                resultLine[3] = stopY + halfSlopeWidth;
            }
        }

        if (-halfWidth <= halfSlopeHeight && halfSlopeHeight <= halfWidth) {
            // line intersects with ...
            if (destination.getY() < startY) {
                // bottom edge
                resultLine[2] = stopX + halfSlopeHeight;
                resultLine[3] = stopY + halfHeight;
            } else if (destination.getY() > startY) {
                // top edge
                resultLine[2] = stopX - halfSlopeHeight;
                resultLine[3] = stopY - halfHeight;
            }
        }

        return resultLine;
    }

    /**
     * Draws a triangle.
     *
     * @param canvas
     * @param paint
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected float[] drawTriangle(Canvas canvas, Paint paint, float x1, float y1, float x2, float y2) {
        final float angle = (float) (Math.atan2(y2 - y1, x2 - x1) + Math.PI);
        final float x3 = (float) (x2 + ARROW_LENGTH * Math.cos(angle - ARROW_DEGREES));
        final float y3 = (float) (y2 + ARROW_LENGTH * Math.sin(angle - ARROW_DEGREES));
        final float x4 = (float) (x2 + ARROW_LENGTH * Math.cos(angle + ARROW_DEGREES));
        final float y4 = (float) (y2 + ARROW_LENGTH * Math.sin(angle + ARROW_DEGREES));

        trianglePath.moveTo(x2, y2); // Top
        trianglePath.lineTo(x3, y3); // Bottom left
        trianglePath.lineTo(x4, y4); // Bottom right
        trianglePath.close();

        canvas.drawPath(trianglePath, paint);

        // calculate centroid of the triangle
        float x = (x2 + x3 + x4) / 3;
        float y = (y2 + y3 + y4) / 3;
        float[] triangleCentroid = new float[]{x, y};

        trianglePath.reset();

        return triangleCentroid;
    }
}
