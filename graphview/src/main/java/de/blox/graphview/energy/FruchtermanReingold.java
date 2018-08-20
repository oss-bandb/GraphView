package de.blox.graphview.energy;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.blox.graphview.Vector;
import de.blox.graphview.Algorithm;
import de.blox.graphview.Edge;
import de.blox.graphview.EdgeRenderer;
import de.blox.graphview.Graph;
import de.blox.graphview.Node;

/**
 *
 */
public class FruchtermanReingold implements Algorithm {
    public static final int DEFAULT_ITERATIONS = 1000;

    private static final double EPSILON = 0.0001D;
    private static final long SEED = 401678L;
    private final EdgeRenderer edgeRenderer = new EnergyEdgeRenderer();
    private final int iterations;
    private Map<Node, Vector> disps = new HashMap<>();
    private Random rand = new Random(SEED);
    private int width;
    private int height;
    private double k;
    private double t;
    private double attraction_k;
    private double repulsion_k;

    public FruchtermanReingold() {
        this(DEFAULT_ITERATIONS);
    }

    public FruchtermanReingold(int iterations) {
        this.iterations = iterations;
    }

    private static int randInt(Random rand, int min, int max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;
    }

    private void randomize(List<Node> nodes) {

        for (Node node : nodes) {
            // create meta data for each node
            disps.put(node, new Vector());
            if (node.getPosition() == null) {
                node.setPos(new Vector(randInt(rand, 0, width), randInt(rand, 0, height)));
            }
        }
    }

    private void cool(int currentIteration) {
        t *= (1.0 - currentIteration / (double) iterations);
    }

    private void limitMaximumDisplacement(List<Node> nodes) {
        for (Node v : nodes) {
            double dispLength = Math.max(EPSILON, getDisp(v).length());
            v.setPos(v.getPosition().add(getDisp(v).divide(dispLength).multiply(Math.min(dispLength, t))));
        }
    }

    private void calculateAttraction(List<Edge> edges) {
        for (Edge e : edges) {
            final Node v = e.getSource();
            final Node u = e.getDestination();

            Vector delta = v.getPosition().subtract(u.getPosition());
            double deltaLength = Math.max(EPSILON, delta.length());
            setDisp(v, getDisp(v).subtract(delta.divide(deltaLength).multiply(forceAttraction(deltaLength))));
            setDisp(u, getDisp(u).add((delta.divide(deltaLength).multiply(forceAttraction(deltaLength)))));
        }
    }

    private void calculateRepulsion(List<Node> nodes) {
        for (Node v : nodes) {
            for (Node u : nodes) {
                if (!u.equals(v)) {
                    Vector delta = v.getPosition().subtract(u.getPosition());
                    double deltaLength = Math.max(EPSILON, delta.length());
                    setDisp(v, getDisp(v).add(delta.divide(deltaLength).multiply(forceRepulsion(deltaLength))));
                }
            }
        }
    }

    private double forceAttraction(double x) {
        return (x * x) / attraction_k;
    }

    private double forceRepulsion(double x) {
        return (repulsion_k * repulsion_k) / x;
    }

    private Vector getDisp(Node node) {
        return disps.get(node);
    }

    private void setDisp(Node node, Vector disp) {
        disps.put(node, disp);
    }

    @Override
    public void run(Graph graph) {
        final List<Node> nodes = graph.getNodes();
        final List<Edge> edges = graph.getEdges();

        t = 0.1 * Math.sqrt(width/2 * height/2);
        k = 0.75 * Math.sqrt(width * height / nodes.size());

        attraction_k = 0.75 * k;
        repulsion_k = 0.75 * k;

        randomize(nodes);

        for (int i = 0; i < iterations; i++) {
            calculateRepulsion(nodes);

            calculateAttraction(edges);

            limitMaximumDisplacement(nodes);

            cool(i);

            if(done()) {
                System.out.println("Done" + i);
                break;
            }
        }
    }

    private boolean done() {
        return t < 1.0 / Math.max(height, width);
    }

    @Override
    public void drawEdges(Canvas canvas, Graph graph, Paint linePaint) {
        edgeRenderer.render(canvas, graph, linePaint);
    }

    @Override
    public void setMeasuredDimension(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
