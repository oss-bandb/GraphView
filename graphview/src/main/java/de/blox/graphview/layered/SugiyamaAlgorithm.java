package de.blox.graphview.layered;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.blox.graphview.Algorithm;
import de.blox.graphview.Edge;
import de.blox.graphview.Graph;
import de.blox.graphview.Node;
import de.blox.graphview.Size;
import de.blox.graphview.edgerenderer.EdgeRenderer;

public class SugiyamaAlgorithm implements Algorithm {
    private final SugiyamaConfiguration configuration;
    private Map<Node, SugiyamaNodeData> nodeData = new HashMap<>();
    private Map<Edge, SugiyamaEdgeData> edgeData = new HashMap<>();
    private Set<Node> stack = new HashSet<>();
    private Set<Node> visited = new HashSet<>();
    private List<List<Node>> layers = new ArrayList<>();
    private Graph graph;
    private EdgeRenderer edgeRenderer;
    private Size size = new Size(0, 0);

    public SugiyamaAlgorithm(SugiyamaConfiguration configuration) {
        this.configuration = configuration;
        edgeRenderer = new SugiyamaEdgeRenderer(nodeData, edgeData);
    }

    @Override
    public void run(Graph graph) {
        this.graph = copyGraph(graph);
        reset();
        initSugiymaData();
        cycleRemoval();
        layerAssignment();
        nodeOrdering();
        coordinateAssignment();
        calculateGraphSize(this.graph);
        denormalize();
        restoreCycle();
    }

    private void calculateGraphSize(Graph graph) {
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
        for (Node node : graph.getNodes()) {
            left = (int) Math.min(left, node.getX());
            top = (int) Math.min(top, node.getY());
            right = (int) Math.max(right, node.getX() + node.getWidth());
            bottom = (int) Math.max(bottom, node.getY() + node.getHeight());
        }

        size = new Size(right - left, bottom - top);
    }

    private void reset() {
        layers.clear();
        stack.clear();
        visited.clear();
        nodeData.clear();
        edgeData.clear();
        nodeCount = 1;
    }

    private void initSugiymaData() {
        for (Node node : graph.getNodes()) {
            node.setX(0f);
            node.setY(0f);
            nodeData.put(node, new SugiyamaNodeData());
        }
        for (Edge edge : graph.getEdges()) {
            edgeData.put(edge, new SugiyamaEdgeData());
        }
    }

    private void cycleRemoval() {
        for (Node node : graph.getNodes()) {
            dfs(node);
        }
    }

    private void dfs(Node node) {
        if (visited.contains(node)) {
            return;
        }

        visited.add(node);
        stack.add(node);

        for (Edge edge : graph.getOutEdges(node)) {
            Node target = edge.getDestination();
            if (stack.contains(target)) {
                graph.removeEdge(edge);
                graph.addEdge(target, node);
                nodeData.get(node).reversed.add(target);
            } else {
                dfs(target);
            }
        }
        stack.remove(node);
    }

    // top sort + add dummy nodes
    private void layerAssignment() {
        if (graph.getNodes().isEmpty()) {
            return;
        }

        // build layers
        final Graph copyGraph = copyGraph(graph);
        List<Node> roots = getRootNodes(copyGraph);
        while (roots.size() > 0) {
            layers.add(roots);
            copyGraph.removeNodes(roots.toArray(new Node[roots.size()]));
            roots = getRootNodes(copyGraph);
        }

        // add dummy's
        for (int i = 0; i < layers.size() - 1; i++) {
            int indexNextLayer = i + 1;
            List<Node> currentLayer = layers.get(i);
            List<Node> nextLayer = layers.get(indexNextLayer);

            for (final Node node : currentLayer) {
                final List<Edge> edges = Stream.ofNullable(this.graph.getEdges())
                        .filter(e -> e.getSource().equals(node))
                        .filter(e -> Math.abs(nodeData.get(e.getDestination()).layer - nodeData.get(node).layer) > 1)
                        .toList();
                final Iterator<Edge> iterator = edges.iterator();
                while (iterator.hasNext()) {
                    Edge edge = iterator.next();
                    final Node dummy = new Node(getDummyText());
                    final SugiyamaNodeData dummyNodeData = new SugiyamaNodeData();
                    dummyNodeData.dummy = true;
                    dummyNodeData.layer = indexNextLayer;
                    nextLayer.add(dummy);
                    nodeData.put(dummy, dummyNodeData);
                    dummy.setSize(edge.getSource().getWidth(), 0); // TODO: calc avg layer height
                    final Edge dummyEdge1 = this.graph.addEdge(edge.getSource(), dummy);
                    final Edge dummyEdge2 = this.graph.addEdge(dummy, edge.getDestination());
                    edgeData.put(dummyEdge1, new SugiyamaEdgeData());
                    edgeData.put(dummyEdge2, new SugiyamaEdgeData());
                    this.graph.removeEdge(edge);

                    iterator.remove();
                }
            }
        }
    }

    private List<Node> getRootNodes(Graph graph) {
        ArrayList<Node> roots = new ArrayList<>();

        for (Node node : graph.getNodes()) {
            int inDegree = 0;
            for (Edge edge : graph.getEdges()) {
                if (edge.getDestination().equals(node)) {
                    inDegree++;
                }
            }
            if (inDegree == 0) {
                roots.add(node);
                nodeData.get(node).layer = layers.size();
            }
        }
        return roots;
    }

    private Graph copyGraph(Graph graph) {
        final Graph copy = new Graph();
        copy.addNodes(graph.getNodes().toArray(new Node[graph.getNodes().size()]));
        copy.addEdges(graph.getEdges().toArray(new Edge[graph.getEdges().size()]));
        return copy;
    }

    private void nodeOrdering() {
        List<List<Node>> best = new ArrayList<>(layers);
        for (int i = 0; i < 24; i++) {
            median(best, i);
            transpose(best);
            if (crossing(best) < crossing(layers)) {
                layers = best;
            }
        }
    }

    private void median(List<List<Node>> layers, int currentIteration) {
        if (currentIteration % 2 == 0) {
            for (int i = 1; i < layers.size(); i++) {
                final List<Node> currentLayer = layers.get(i);
                final List<Node> previousLayer = layers.get(i - 1);
                for (Node node : currentLayer) {
                    final List<Integer> positions = Stream.of(graph.getEdges())
                            .filter(edge -> previousLayer.contains(edge.getSource()))
                            .map(edge -> previousLayer.indexOf(edge.getSource())).toList();
                    Collections.sort(positions);
                    int median = positions.size() / 2;
                    if (!positions.isEmpty()) {
                        if (positions.size() == 1) {
                            nodeData.get(node).median = -1;
                        } else if (positions.size() == 2) {
                            nodeData.get(node).median = (positions.get(0) + positions.get(1)) / 2;
                        } else if (positions.size() % 2 == 1) {
                            nodeData.get(node).median = positions.get(median);
                        } else {
                            int left = positions.get(median - 1) - positions.get(0);
                            int right = positions.get(positions.size() - 1) - positions.get(median);
                            if (left + right != 0) {
                                nodeData.get(node).median = (positions.get(median - 1) * right + positions.get(median) * left) / (left + right);
                            }
                        }
                    }
                }
                Collections.sort(currentLayer, (n1, n2) -> {
                    final SugiyamaNodeData nodeData1 = nodeData.get(n1);
                    final SugiyamaNodeData nodeData2 = nodeData.get(n2);
                    return nodeData1.median - nodeData2.median;
                });
            }
        } else {
            for (int l = 1; l < layers.size(); l++) {
                final List<Node> currentLayer = layers.get(l);
                final List<Node> previousLayer = layers.get(l - 1);
                for (int i = currentLayer.size() - 1; i > 0; i--) {
                    final Node node = currentLayer.get(i);
                    final List<Integer> positions = Stream.of(graph.getEdges())
                            .filter(edge -> previousLayer.contains(edge.getSource()))
                            .map(edge -> previousLayer.indexOf(edge.getSource())).toList();
                    Collections.sort(positions);
                    if (!positions.isEmpty()) {
                        if (positions.size() == 1) {
                            nodeData.get(node).median = positions.get(0);
                        } else {
                            nodeData.get(node).median = (positions.get((int) Math.ceil(positions.size() / 2.0)) + positions.get((int) Math.ceil(positions.size() / 2.0) - 1)) / 2;
                        }
                    }
                }
                Collections.sort(currentLayer, (n1, n2) -> {
                    final SugiyamaNodeData nodeData1 = nodeData.get(n1);
                    final SugiyamaNodeData nodeData2 = nodeData.get(n2);
                    return nodeData1.median - nodeData2.median;
                });
            }
        }
    }

    private void transpose(List<List<Node>> layers) {
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int l = 0; l < layers.size() - 1; l++) {
                final List<Node> northernNodes = layers.get(l);
                final List<Node> southernNodes = layers.get(l + 1);
                for (int i = 0; i < southernNodes.size() - 1; i++) {
                    Node v = southernNodes.get(i);
                    Node w = southernNodes.get(i + 1);
                    if (crossing(northernNodes, v, w) > crossing(northernNodes, w, v)) {
                        improved = true;
                        exchange(southernNodes, v, w);
                    }
                }
            }
        }
    }

    private void exchange(List<Node> nodes, Node v, Node w) {
        Collections.swap(nodes, nodes.indexOf(v), nodes.indexOf(w));
    }

    // counts the number of edge crossings if n2 appears to the left of n1 in their layer.
    private int crossing(List<Node> northernNodes, Node n1, Node n2) {
        int crossing = 0;

        final List<Node> parentNodesN1 = Stream.ofNullable(graph.getEdges())
                .filter(edge -> edge.getDestination().equals(n1))
                .map(Edge::getSource)
                .toList();

        final List<Node> parentNodesN2 = Stream.ofNullable(graph.getEdges())
                .filter(edge -> edge.getDestination().equals(n2))
                .map(Edge::getSource)
                .toList();

        for (Node pn2 : parentNodesN2) {
            final int indexOfPn2 = northernNodes.indexOf(pn2);
            for (Node pn1 : parentNodesN1) {
                if (indexOfPn2 < northernNodes.indexOf(pn1)) {
                    crossing++;
                }
            }
        }
        return crossing;
    }

    private int crossing(List<List<Node>> layers) {
        int crossing = 0;
        for (int l = 0; l < layers.size() - 1; l++) {
            final List<Node> southernNodes = layers.get(l);
            final List<Node> northernNodes = layers.get(l + 1);
            for (int i = 0; i < southernNodes.size() - 2; i++) {
                Node v = southernNodes.get(i);
                Node w = southernNodes.get(i + 1);
                crossing += crossing(northernNodes, v, w);
            }
        }
        return crossing;
    }

    private void coordinateAssignment() {
        assignX();
        assignY();
    }

    private void assignX() {
        // each node points to the root of the block.
        List<HashMap<Node, Node>> root = new ArrayList<>(4);
        // each node points to its aligned neighbor in the layer below.
        List<HashMap<Node, Node>> align = new ArrayList<>(4);
        List<HashMap<Node, Node>> sink = new ArrayList<>(4);
        List<HashMap<Node, Float>> x = new ArrayList<>(4);
        // minimal separation between the roots of different classes.
        List<HashMap<Node, Float>> shift = new ArrayList<>(4);
        // the width of each block (max width of node in block)
        List<HashMap<Node, Float>> blockWidth = new ArrayList<>(4);


        for (int i = 0; i < 4; i++) {
            root.add(new HashMap<>());
            align.add(new HashMap<>());
            sink.add(new HashMap<>());
            shift.add(new HashMap<>());
            x.add(new HashMap<>());
            blockWidth.add(new HashMap<>());
            for (Node n : graph.getNodes()) {
                root.get(i).put(n, n);
                align.get(i).put(n, n);
                sink.get(i).put(n, n);
                shift.get(i).put(n, Float.MAX_VALUE);
                x.get(i).put(n, Float.MIN_VALUE);
                blockWidth.get(i).put(n, 0f);
            }
        }
        // calc the layout for down/up and leftToRight/rightToLeft
        for (int downward = 0; downward <= 1; downward++) {
            final List<List<Boolean>> type1Conflicts = markType1Conflicts(downward == 0);
            for (int leftToRight = 0; leftToRight <= 1; leftToRight++) {
                int k = 2 * downward + leftToRight;

                verticalAlignment(root.get(k), align.get(k), type1Conflicts, downward == 0, leftToRight == 0);
                computeBlockWidths(root.get(k), blockWidth.get(k));
                horizontalCompactation(align.get(k), root.get(k), sink.get(k), shift.get(k), blockWidth.get(k), x.get(k), leftToRight == 0, downward == 0);
            }
        }
        balance(x, blockWidth);
    }

    private void balance(List<HashMap<Node, Float>> x, List<HashMap<Node, Float>> blockWidth) {
        HashMap<Node, Float> coordinates = new HashMap<>();

        float minWidth = Float.MAX_VALUE;
        int smallestWidthLayout = 0;
        float[] min = new float[4];
        float[] max = new float[4];

        // get the layout with smallest width and set minimum and maximum value
        // for each direction
        for (int i = 0; i <= 3; ++i) {
            min[i] = Integer.MAX_VALUE;
            max[i] = 0;
            for (Node v : graph.getNodes()) {
                float bw = 0.5f * blockWidth.get(i).get(v);
                float xp = x.get(i).get(v) - bw;
                if (xp < min[i]) {
                    min[i] = xp;
                }
                xp = x.get(i).get(v) + bw;
                if (xp > max[i]) {
                    max[i] = xp;
                }
            }
            float width = max[i] - min[i];
            if (width < minWidth) {
                minWidth = width;
                smallestWidthLayout = i;
            }
        }

        // align the layouts to the one with smallest width
        for (int i = 0; i <= 3; ++i) {
            if (i != smallestWidthLayout) {

                // align the left to right layouts to the left border of the
                // smallest layout
                if (i == 0 || i == 1) {
                    float diff = min[i] - min[smallestWidthLayout];
                    for (Node n : x.get(i).keySet()) {
                        if (diff > 0) {
                            x.get(i).put(n, x.get(i).get(n)
                                    - diff);
                        } else {
                            x.get(i).put(n, x.get(i).get(n)
                                    + diff);
                        }
                    }

                    // align the right to left layouts to the right border of
                    // the smallest layout
                } else {
                    float diff = max[i] - max[smallestWidthLayout];
                    for (Node n : x.get(i).keySet()) {
                        if (diff > 0) {
                            x.get(i).put(n, x.get(i).get(n)
                                    - diff);
                        } else {
                            x.get(i).put(n, x.get(i).get(n)
                                    + diff);
                        }
                    }
                }
            }
        }

        // get the minimum coordinate value
        float minValue = Float.MAX_VALUE;
        for (int i = 0; i <= 3; ++i) {
            for (float xVal : x.get(i).values()) {
                if (xVal < minValue) {
                    minValue = xVal;
                }
            }
        }

        // set left border to 0
        if (minValue != 0) {
            for (int i = 0; i <= 3; ++i) {
                for (Node n : x.get(i).keySet()) {
                    x.get(i).put(n, x.get(i).get(n) - minValue);
                }
            }
        }

        // get the average median of each coordinate
        for (Node n : this.graph.getNodes()) {
            float[] values = new float[4];
            for (int i = 0; i < 4; i++) {
                values[i] = x.get(i).get(n);
            }
            Arrays.sort(values);
            float average = (values[1] + values[2]) / 2;
            coordinates.put(n, average);
        }

        // get the minimum coordinate value
        minValue = Integer.MAX_VALUE;

        for (float xVal : coordinates.values()) {
            if (xVal < minValue) {
                minValue = xVal;
            }
        }

        // set left border to 0
        if (minValue != 0) {

            for (Node n : coordinates.keySet()) {
                coordinates.put(n, coordinates.get(n) - minValue);
            }
        }

        for (Node v : graph.getNodes()) {
            v.setX(coordinates.get(v));
        }
    }

    private List<List<Boolean>> markType1Conflicts(boolean downward) {

        List<List<Boolean>> type1Conflicts = new ArrayList<>();

        for (int i = 0; i < graph.getNodes().size(); i++) {
            type1Conflicts.add(new ArrayList<>());
            for (int l = 0; l < graph.getEdges().size(); l++) {
                type1Conflicts.get(i).add(false);
            }
        }


        if (layers.size() >= 4) {
            int upper, lower; // iteration bounds
            int k1; // node position boundaries of closest inner segments
            if (downward) {
                lower = 1;
                upper = layers.size() - 2;
            } else {
                lower = layers.size() - 1;
                upper = 2;
            }

            /*
             * iterate level[2..h-2] in the given direction
             *
             * availible levels: 1 to h
             */
            for (int i = lower; (downward && i <= upper) || (!downward && i >= upper); i = downward ? i + 1 : i - 1) {
                int k0 = 0;
                int firstIndex = 0; // index of first node on layer
                final List<Node> currentLevel = layers.get(i);
                final List<Node> nextLevel = downward ? layers.get(i + 1) : layers.get(i - 1);

                // for all nodes on next level
                for (int l1 = 0; l1 <= nextLevel.size() - 1; l1++) {
                    Node virtualTwin = virtualTwinNode(nextLevel.get(l1), downward);
                    if (l1 == nextLevel.size() - 1 || virtualTwin != null) {
                        k1 = currentLevel.size() - 1;

                        if (virtualTwin != null) {
                            k1 = pos(virtualTwin);
                        }

                        for (; firstIndex <= l1; firstIndex++) {
                            final List<Node> upperNeighbours = getAdjNodes(nextLevel.get(l1), downward);

                            for (Node currentNeighbour : upperNeighbours) {
                                /*
                                 * XXX: < 0 in first iteration is still ok for indizes starting
                                 * with 0 because no index can be smaller than 0
                                 */
                                final int currentNeighbourIndex = pos(currentNeighbour);
                                if (currentNeighbourIndex < k0 || currentNeighbourIndex > k1) {
                                    type1Conflicts.get(l1).set(currentNeighbourIndex, true);
                                }
                            }
                        }
                        k0 = k1;
                    }
                }
            }
        }
        return type1Conflicts;
    }


    private void verticalAlignment(HashMap<Node, Node> root, HashMap<Node, Node> align, List<List<Boolean>> type1Conflicts, boolean downward, boolean leftToRight) {
        // for all Level
        for (int i = downward ? 0 : layers.size() - 1;
             (downward && i <= layers.size() - 1) || (!downward && i >= 0);
             i = downward ? i + 1 : i - 1) {
            List<Node> currentLevel = layers.get(i);
            int r = leftToRight ? -1 : Integer.MAX_VALUE;
            // for all nodes on Level i (with direction leftToRight)
            for (int k = leftToRight ? 0 : currentLevel.size() - 1;
                 (leftToRight && k <= currentLevel.size() - 1) || (!leftToRight && k >= 0);
                 k = leftToRight ? k + 1 : k - 1) {

                Node v = currentLevel.get(k);
                final List<Node> adjNodes = getAdjNodes(v, downward);
                if (adjNodes.size() > 0) {
                    // the first median
                    int median = (int) Math.floor((adjNodes.size() + 1) / 2.0);
                    int medianCount = (adjNodes.size() % 2 == 1) ? 1 : 2;

                    // for all median neighbours in direction of H
                    for (int count = 0; count < medianCount; count++) {
                        Node m = adjNodes.get(median + count - 1);
                        final int posM = pos(m);

                        if (align.get(v).equals(v)
                                // if segment (u,v) not marked by type1 conflicts AND ...
                                && !type1Conflicts.get(pos(v)).get(posM)
                                && ((leftToRight && r < posM)
                                || (!leftToRight && r > posM))) {
                            align.put(m, v);
                            root.put(v, root.get(m));
                            align.put(v, root.get(v));
                            r = posM;
                        }
                    }
                }
            }
        }
    }

    private void computeBlockWidths(HashMap<Node, Node> root, HashMap<Node, Float> blockWidth) {
        for (Node v : graph.getNodes()) {
            Node r = root.get(v);
            blockWidth.put(r, Math.max(blockWidth.get(r), v.getWidth()));
        }
    }

    private void horizontalCompactation(HashMap<Node, Node> align, HashMap<Node, Node> root, HashMap<Node, Node> sink, HashMap<Node, Float> shift, HashMap<Node, Float> blockWidth, HashMap<Node, Float> x, boolean leftToRight, boolean downward) {

        // calculate class relative coordinates for all roots
        for (int i = downward ? 0 : layers.size() - 1;
             (downward && i <= layers.size() - 1) || (!downward && i >= 0);
             i = downward ? i + 1 : i - 1) {
            final List<Node> currentLevel = layers.get(i);

            for (int j = leftToRight ? 0 : currentLevel.size() - 1;
                 (leftToRight && j <= currentLevel.size() - 1) || (!leftToRight && j >= 0);
                 j = leftToRight ? j + 1 : j - 1) {
                Node v = currentLevel.get(j);
                if (root.get(v).equals(v)) {
                    placeBlock(v, sink, shift, x, align, blockWidth, root, leftToRight);
                }
            }
        }
        float d = 0f;
        for (int i = downward ? 0 : layers.size() - 1;
             (downward && i <= layers.size() - 1) || (!downward && i >= 0);
             i = downward ? i + 1 : i - 1) {
            final List<Node> currentLevel = layers.get(i);

            Node v = currentLevel.get(leftToRight ? 0 : currentLevel.size() - 1);

            if (v.equals(sink.get(root.get(v)))) {
                Float oldShift = shift.get(v);
                if (oldShift < Float.MAX_VALUE) {
                    shift.put(v, oldShift + d);
                    d += oldShift;
                } else {
                    shift.put(v, 0f);
                }
            }
        }

        // apply root coordinates for all aligned nodes
        // (place block did this only for the roots)+
        for (Node v : graph.getNodes()) {
            x.put(v, x.get(root.get(v)));

            final Float shiftVal = shift.get(sink.get(root.get(v)));
            if (shiftVal < Float.MAX_VALUE) {
                x.put(v, x.get(v) + shiftVal);  // apply shift for each class
            }
        }
    }

    private void placeBlock(Node v, HashMap<Node, Node> sink, HashMap<Node, Float> shift, HashMap<Node, Float> x, HashMap<Node, Node> align, HashMap<Node, Float> blockWidth, HashMap<Node, Node> root, boolean leftToRight) {
        if (x.get(v).equals(Float.MIN_VALUE)) {
            x.put(v, 0f);
            Node w = v;
            do {
                // if not first node on layer
                if ((leftToRight && pos(w) > 0) || (!leftToRight && pos(w) < layers.get(getLayerIndex(w)).size() - 1)) {
                    final Node pred = pred(w, leftToRight);
                    Node u = root.get(pred);

                    placeBlock(u, sink, shift, x, align, blockWidth, root, leftToRight);

                    if (sink.get(v).equals(v)) {
                        sink.put(v, sink.get(u));
                    }
                    if (!sink.get(v).equals(sink.get(u))) {

                        if (leftToRight) {
                            shift.put(sink.get(u), Math.min(shift.get(sink.get(u)), x.get(v) - x.get(u) - configuration.getNodeSeparation() - 0.5f * (blockWidth.get(u) + blockWidth.get(v))));
                        } else {
                            shift.put(sink.get(u), Math.max(shift.get(sink.get(u)), x.get(v) - x.get(u) + configuration.getNodeSeparation() + 0.5f * (blockWidth.get(u) + blockWidth.get(v))));
                        }
                    } else {
                        if (leftToRight) {
                            x.put(v, Math.max(x.get(v), x.get(u) + configuration.getNodeSeparation() + 0.5f * (blockWidth.get(u) + blockWidth.get(v))));
                        } else {
                            x.put(v, Math.min(x.get(v), x.get(u) - configuration.getNodeSeparation() - 0.5f * (blockWidth.get(u) + blockWidth.get(v))));
                        }
                    }

                }
                w = align.get(w);
            } while (!w.equals(v));
        }
    }

    // predecessor
    private Node pred(Node v, boolean leftToRight) {
        int pos = pos(v);
        int rank = getLayerIndex(v);

        final List<Node> level = layers.get(rank);
        if ((leftToRight && pos != 0) || (!leftToRight && pos != level.size() - 1)) {
            return level.get(leftToRight ? pos - 1 : pos + 1);
        }
        return null;
    }


    private Node virtualTwinNode(Node node, boolean downward) {

        if (!isLongEdgeDummy(node)) {
            return null;
        }
        final List<Node> adjNodes = getAdjNodes(node, downward);

        if (adjNodes.size() == 0) {
            return null;
        }

        return adjNodes.get(0);
    }


    private List<Node> getAdjNodes(Node node, boolean downward) {
        return (downward ? graph.predecessorsOf(node) : graph.successorsOf(node));
    }

    // get node index in layer
    private int pos(Node node) {
        for (List<Node> l : layers) {
            for (Node n : l) {
                if (node.equals(n)) {
                    return l.indexOf(node);
                }
            }
        }
        return -1; // or exception?
    }

    private int getLayerIndex(Node node) {
        for (int l = 0; l < layers.size(); l++) {
            for (Node n : layers.get(l)) {
                if (node.equals(n)) {
                    return l;
                }
            }
        }
        return -1; // or exception?
    }

    private boolean isLongEdgeDummy(Node v) {
        final List<Node> successors = graph.successorsOf(v);
        return nodeData.get(v).isDummy() && successors.size() == 1 && nodeData.get(successors.get(0)).isDummy();
    }

    private void assignY() {
        // compute y-coordinates
        int k = layers.size();

        // compute height of each layer
        Float[] data = new Float[graph.getNodes().size()];
        Arrays.fill(data, 0f);
        List<Float> height = new ArrayList<>(Arrays.asList(data));

        for (int i = 0; i < k; ++i) {
            final List<Node> level = layers.get(i);
            for (int j = 0; j < level.size(); ++j) {
                final Node node = level.get(j);
                float h = nodeData.get(node).isDummy() ? 0.f : node.getHeight();
                if (h > height.get(i))
                    height.set(i, h);
            }
        }

        // assign y-coordinates
        float yPos = 0f;

        for (int i = 0; ; ++i) {
            final List<Node> level = layers.get(i);
            for (int j = 0; j < level.size(); ++j)
                level.get(j).setY(yPos);

            if (i == k - 1)
                break;

            yPos += configuration.getLevelSeparation() + 0.5 * (height.get(i) + height.get(i + 1));
        }
    }

    private void denormalize() {
        // remove dummy's
        for (int i = 1; i < layers.size() - 1; i++) {
            final Iterator<Node> iterator = layers.get(i).iterator();
            while (iterator.hasNext()) {
                Node current = iterator.next();
                if (nodeData.get(current).isDummy()) {

                    final Node predecessor = graph.predecessorsOf(current).get(0);
                    final Node successor = graph.successorsOf(current).get(0);

                    final List<Float> bendPoints = edgeData.get(graph.getEdge(predecessor, current)).bendPoints;

                    if (bendPoints.isEmpty()
                            || !(bendPoints.contains(current.getX() + predecessor.getWidth() / 2f))) {
                        bendPoints.add(predecessor.getX() + predecessor.getWidth() / 2f);
                        bendPoints.add(predecessor.getY() + predecessor.getHeight() / 2f);

                        bendPoints.add(current.getX() + predecessor.getWidth() / 2f);
                        bendPoints.add(current.getY());
                    }

                    if (!nodeData.get(predecessor).isDummy()) {
                        bendPoints.add(current.getX() + predecessor.getWidth() / 2f);
                    } else {
                        bendPoints.add(current.getX());
                    }
                    bendPoints.add(current.getY());

                    if (nodeData.get(successor).isDummy()) {
                        bendPoints.add(successor.getX() + predecessor.getWidth() / 2f);
                    } else {
                        bendPoints.add(successor.getX() + successor.getWidth() / 2f);
                    }
                    bendPoints.add(successor.getY() + successor.getHeight() / 2f);

                    graph.removeEdge(predecessor, current);
                    graph.removeEdge(current, successor);
                    final Edge edge = graph.addEdge(predecessor, successor);
                    SugiyamaEdgeData sugiyamaEdgeData = new SugiyamaEdgeData();
                    sugiyamaEdgeData.bendPoints = bendPoints;
                    edgeData.put(edge, sugiyamaEdgeData);

                    iterator.remove();
                    graph.removeNode(current);
                }
            }
        }
    }

    private void restoreCycle() {
        for (Node n : graph.getNodes()) {
            if (nodeData.get(n).isReversed()) {
                for (Node target : nodeData.get(n).reversed) {
                    final List<Float> bendPoints = edgeData.get(graph.getEdge(target, n)).bendPoints;
                    graph.removeEdge(target, n);
                    final Edge edge = graph.addEdge(n, target);
                    final SugiyamaEdgeData edgeData = new SugiyamaEdgeData();
                    edgeData.bendPoints = bendPoints;
                    this.edgeData.put(edge, edgeData);
                }
            }
        }
    }

    @Override
    public void drawEdges(Canvas canvas, Graph graph, Paint mLinePaint) {
        edgeRenderer.render(canvas, this.graph, mLinePaint);
    }

    @Override
    public void setEdgeRenderer(EdgeRenderer renderer) {
        throw new UnsupportedOperationException("SugiyamaAlgorithm currently not support custom edge renderer!");
    }

    @Override
    public Size getGraphSize() {
        return size;
    }

    private int nodeCount = 1;

    private String getDummyText() {
        return "Dummy " + nodeCount++;
    }
}
