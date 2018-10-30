package de.blox.graphview.energy;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.blox.graphview.Algorithm;
import de.blox.graphview.Edge;
import de.blox.graphview.EdgeRenderer;
import de.blox.graphview.Graph;
import de.blox.graphview.Node;
import de.blox.graphview.Vector;

/**
 *
 */
public class FruchtermanReingoldAlgorithm implements Algorithm {
    public static final int DEFAULT_ITERATIONS = 1000;
    public static final int CLUSTER_PADDING = 100;
    private static final double EPSILON = 0.0001D;
    private static final long SEED = 401678L;
    private final int iterations;
    private EdgeRenderer edgeRenderer = new EnergyEdgeRenderer();
    private Map<Node, Vector> disps = new HashMap<>();
    private Random rand = new Random(SEED);
    private int width;
    private int height;
    private float k;
    private float t;
    private float attraction_k;
    private float repulsion_k;

    public FruchtermanReingoldAlgorithm() {
        this(DEFAULT_ITERATIONS);
    }

    public FruchtermanReingoldAlgorithm(int iterations) {
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
            node.setPos(new Vector(randInt(rand, 0, width / 2), randInt(rand, 0, height / 2)));
        }
    }

    private void cool(int currentIteration) {
        t *= (1.0 - currentIteration / (double) iterations);
    }

    private void limitMaximumDisplacement(List<Node> nodes) {
        for (Node v : nodes) {
            float dispLength = (float) Math.max(EPSILON, getDisp(v).length());
            v.setPos(v.getPosition().add(getDisp(v).divide(dispLength).multiply((float) Math.min(dispLength, t))));
        }
    }

    private void calculateAttraction(List<Edge> edges) {
        for (Edge e : edges) {
            final Node v = e.getSource();
            final Node u = e.getDestination();

            Vector delta = v.getPosition().subtract(u.getPosition());
            float deltaLength = (float) Math.max(EPSILON, delta.length());
            setDisp(v, getDisp(v).subtract(delta.divide(deltaLength).multiply(forceAttraction(deltaLength))));
            setDisp(u, getDisp(u).add((delta.divide(deltaLength).multiply(forceAttraction(deltaLength)))));
        }
    }

    private void calculateRepulsion(List<Node> nodes) {
        for (Node v : nodes) {
            for (Node u : nodes) {
                if (!u.equals(v)) {
                    Vector delta = v.getPosition().subtract(u.getPosition());
                    float deltaLength = (float) Math.max(EPSILON, delta.length());
                    setDisp(v, getDisp(v).add(delta.divide(deltaLength).multiply(forceRepulsion(deltaLength))));
                }
            }
        }
    }

    private float forceAttraction(float x) {
        return (x * x) / attraction_k;
    }

    private float forceRepulsion(float x) {
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
        final int size = findBiggestSize(graph) * graph.getNodeCount();
        width = size;
        height = size;

        final List<Node> nodes = graph.getNodes();
        final List<Edge> edges = graph.getEdges();

        t = (float) (0.1 * Math.sqrt(width / 2 * height / 2));
        k = (float) (0.75 * Math.sqrt(width * height / nodes.size()));

        attraction_k = 0.75f * k;
        repulsion_k = 0.75f * k;

        randomize(nodes);

        for (int i = 0; i < iterations; i++) {
            calculateRepulsion(nodes);

            calculateAttraction(edges);

            limitMaximumDisplacement(nodes);

            cool(i);

            if (done()) {
                break;
            }
        }

        positionNodes(graph);
    }

    private void positionNodes(Graph graph) {
        Vector offset = getOffset(graph);
        List<Node> nodesVisited = new ArrayList<>();
        List<NodeCluster> nodeClusters = new ArrayList<>();
        for (Node node : graph.getNodes()) {
            node.setPos(new Vector(node.getX() - offset.getX(), node.getY() - offset.getY()));
        }

        for (Node node : graph.getNodes()) {
            if (nodesVisited.contains(node)) {
                continue;
            }

            nodesVisited.add(node);
            NodeCluster cluster = findClusterOf(nodeClusters, node);
            if (cluster == null) {
                cluster = new NodeCluster();
                cluster.add(node);
                nodeClusters.add(cluster);
            }

            followEdges(graph, cluster, node, nodesVisited);
        }

        positionCluster(nodeClusters);
    }

    private void positionCluster(List<NodeCluster> nodeClusters) {
        combineSingleNodeCluster(nodeClusters);

        NodeCluster cluster = nodeClusters.get(0);
        // move first cluster to 0,0
        cluster.offset(-cluster.rect.left, -cluster.rect.top);

        for (int i = 1; i < nodeClusters.size(); i++) {
            final NodeCluster nextCluster = nodeClusters.get(i);
            final float xDiff = nextCluster.rect.left - cluster.rect.right - CLUSTER_PADDING;
            final float yDiff = nextCluster.rect.top - cluster.rect.top;
            nextCluster.offset(-xDiff, -yDiff);
            cluster = nextCluster;
        }
    }


    private void combineSingleNodeCluster(List<NodeCluster> nodeClusters) {
        NodeCluster firstSingleNodeCluster = null;
        final Iterator<NodeCluster> iterator = nodeClusters.iterator();
        while (iterator.hasNext()) {
            NodeCluster cluster = iterator.next();
            if (cluster.size() == 1) {
                if (firstSingleNodeCluster == null) {
                    firstSingleNodeCluster = cluster;
                    continue;
                }

                firstSingleNodeCluster.concat(cluster);
                iterator.remove();
            }
        }
    }

    private void followEdges(Graph graph, NodeCluster cluster, Node node, List<Node> nodesVisited) {
        for (Node successor : graph.successorsOf(node)) {
            if (nodesVisited.contains(successor)) {
                continue;
            }

            nodesVisited.add(successor);
            cluster.add(successor);

            followEdges(graph, cluster, successor, nodesVisited);
        }

        for (Node predecessor : graph.predecessorsOf(node)) {
            if (nodesVisited.contains(predecessor)) {
                continue;
            }

            nodesVisited.add(predecessor);
            cluster.add(predecessor);

            followEdges(graph, cluster, predecessor, nodesVisited);
        }
    }

    private NodeCluster findClusterOf(List<NodeCluster> clusters, Node node) {
        for (NodeCluster cluster : clusters) {
            if (cluster.contains(node)) {
                return cluster;
            }
        }

        return null;
    }

    private int findBiggestSize(Graph graph) {
        int size = 0;
        for (Node node : graph.getNodes()) {
            size = Math.max(size, Math.max(node.getHeight(), node.getWidth()));
        }

        return size;
    }

    private Vector getOffset(Graph graph) {
        float offsetX = Float.MAX_VALUE;
        float offsetY = Float.MAX_VALUE;
        for (Node node : graph.getNodes()) {
            offsetX = Math.min(offsetX, node.getX());
            offsetY = Math.min(offsetY, node.getY());
        }
        return new Vector(offsetX, offsetY);
    }

    private boolean done() {
        return t < 1.0 / Math.max(height, width);
    }

    @Override
    public void drawEdges(Canvas canvas, Graph graph, Paint linePaint) {
        edgeRenderer.render(canvas, graph, linePaint);
    }

    @Override
    public void setEdgeRenderer(EdgeRenderer renderer) {
        this.edgeRenderer = renderer;
    }

    private static class NodeCluster {
        private List<Node> nodes = new ArrayList<>();
        private RectF rect;

        public void add(Node node) {
            nodes.add(node);

            if (rect == null) {
                rect = new RectF(node.getX(), node.getY(), node.getX() + node.getWidth(), node.getY() + node.getHeight());
            } else {
                rect.left = Math.min(rect.left, node.getX());
                rect.top = Math.min(rect.top, node.getY());
                rect.right = Math.max(rect.right, node.getX() + node.getWidth());
                rect.bottom = Math.max(rect.bottom, node.getY() + node.getHeight());
            }
        }

        public boolean contains(Node node) {
            return nodes.contains(node);
        }

        public int size() {
            return nodes.size();
        }

        public void concat(NodeCluster cluster) {
            for (Node node : cluster.nodes) {
                node.setPos(new Vector(rect.right + CLUSTER_PADDING, rect.top));
                add(node);
            }
        }

        public void offset(float xDiff, float yDiff) {
            for (Node node : nodes) {
                node.setPos(node.getPosition().add(xDiff, yDiff));
            }

            rect.offset(xDiff, yDiff);
        }
    }
}
