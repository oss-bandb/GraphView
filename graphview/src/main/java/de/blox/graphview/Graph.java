package de.blox.graphview;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class Graph {
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    private List<NodeObserver> observers = new ArrayList<>();
    private boolean isTree = true;

    /**
     * Add one {@link Node} to the graph without a connection the other nodes.
     *
     * @param node
     */
    public void addNode(@NonNull Node node) {
        if (!this.nodes.contains(node)) {
            this.nodes.add(node);
        }
    }

    /**
     * Add one or more {@link Node Nodes} to the graph without a connection the other nodes.
     *
     * @param nodes
     */
    public void addNodes(@NonNull Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            addNode(nodes[i]);
        }
    }

    /**
     * Remove a {@link Node} from the graph. If the node has a connection with an other node, the connection
     * will be removed too.
     *
     * @param node
     */
    public void removeNode(@NonNull Node node) {
        removeNodeInternal(node);

        for (NodeObserver observer : observers) {
            observer.notifyNodeRemoved(node);
        }
    }

    private void removeNodeInternal(Node node) {
        if (!nodes.contains(node)) {
            throw new IllegalArgumentException("Unable to find node in graph.");
        }

        if (isTree) {
            for (Node n : successorsOf(node)) {
                removeNodeInternal(n);
            }
        }

        nodes.remove(node);

        final Iterator<Edge> iterator = edges.iterator();
        while (iterator.hasNext()) {
            Edge edge = iterator.next();
            if (edge.getSource().equals(node) || edge.getDestination().equals(node)) {
                iterator.remove();
            }
        }

    }

    /**
     * Remove {@link Node Nodes} from the graph. If the node has a connection with an other node, the connection
     * will be removed too.
     *
     * @param nodes
     */
    public void removeNodes(@NonNull Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            removeNode(nodes[i]);
        }
    }

    /**
     * Add an edge between two {@link Node Nodes}. Both nodes will be added to the graph, if the graph
     * doesn't contain these nodes already (you don't have to use {@link #addNode(Node)} anymore).
     *
     * @param source
     * @param destination
     */
    public void addEdge(@NonNull Node source, @NonNull Node destination) {
        addNode(source);
        addNode(destination);

        edges.add(new Edge(source, destination));

        for (NodeObserver observer : observers) {
            observer.notifyInvalidated();
        }
    }

    public void addNodeObserver(NodeObserver nodeObserver) {
        observers.add(nodeObserver);
    }

    public void removeNodeObserver(NodeObserver nodeObserver) {
        observers.remove(nodeObserver);
    }

    public boolean hasNodes() {
        return !nodes.isEmpty();
    }

    /**
     * Returns the node at {@code position}.
     *
     * @param position
     * @return
     */
    public Node getNode(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("position can't be negative");
        }

        final int size = nodes.size();
        if (position >= size) {
            throw new IndexOutOfBoundsException("Position: " + position + ", Size: " + size);
        }

        return nodes.get(position);
    }

    /**
     * Returns the number of nodes in this graph.
     *
     * @return
     */
    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * Returns all nodes in this graph.
     *
     * @return
     */
    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * Returns all edges in this graph.
     *
     * @return
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * @param node
     * @return
     */
    public boolean hasSuccessor(Node node) {
        for (Edge edge : edges) {
            if (edge.getSource().equals(node)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds all successors of {@code node}.
     *
     * @param node
     * @return
     */
    public List<Node> successorsOf(Node node) {
        List<Node> successors = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getSource().equals(node)) {
                successors.add(edge.getDestination());
            }
        }

        return successors;
    }

    /**
     * @param node
     * @return
     */
    public boolean hasPredecessor(Node node) {
        for (Edge edge : edges) {
            if (edge.getDestination().equals(node)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds all predecessors of {@code node}.
     *
     * @param node
     * @return
     */
    public List<Node> predecessorsOf(Node node) {
        List<Node> predecessors = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getDestination().equals(node)) {
                predecessors.add(edge.getSource());
            }
        }

        return predecessors;
    }

    public boolean contains(Node node) {
        return nodes.contains(node);
    }

    public boolean containsData(Object data) {
        for (Node node : nodes) {
            if (node.getData().equals(data)) {
                return true;
            }
        }

        return false;
    }

    public Node getNode(Object data) {
        for (Node node : nodes) {
            if (node.getData().equals(data)) {
                return node;
            }
        }

        return null;
    }

    // Todo this is a quick fix and should be removed later
    public void setAsTree(boolean isTree) {
        this.isTree = isTree;
    }
}
