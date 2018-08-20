package de.blox.graphview.tree;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.List;

import de.blox.graphview.EdgeRenderer;
import de.blox.graphview.Graph;
import de.blox.graphview.Node;

/**
 *
 */
class TreeEdgeRenderer implements EdgeRenderer {

    private int levelSeparation;

    public TreeEdgeRenderer(int levelSeparation) {
        this.levelSeparation = levelSeparation;
    }

    @Override
    public void render(Canvas canvas, Graph graph, Paint paint) {
        Path linePath = new Path();

        List<Node> nodes = graph.getNodes();

        for (Node node : nodes) {
            List<Node> children = graph.findSuccessors(node);

            for (Node child : children) {
                linePath.moveTo((float) child.getX() + (child.getWidth() / 2), (float) child.getY());
                linePath.lineTo((float) child.getX() + (child.getWidth() / 2), (float) child.getY() - (levelSeparation / 2));
                linePath.lineTo((float) node.getX() + (node.getWidth() / 2),
                        (float) child.getY() - levelSeparation / 2);

                canvas.drawPath(linePath, paint);
                linePath.reset();

                linePath.moveTo((float) node.getX() + (node.getWidth() / 2),
                        (float) child.getY() - levelSeparation / 2);
                linePath.lineTo((float) node.getX() + (node.getWidth() / 2),
                        (float) node.getY() + node.getHeight());

                canvas.drawPath(linePath, paint);
            }
        }
    }
}
