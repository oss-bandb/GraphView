package de.blox.graphview;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Graph {
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    private List<NodeObserver> observers = new ArrayList<>();
    private boolean isTree = false;

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
    public Edge addEdge(@NonNull Node source, @NonNull Node destination) {
        final Edge edge = new Edge(source, destination);
        addEdge(edge);

        return edge;
    }

    /**
     * Add one {@link Edge} to the graph.
     *
     * @param edge
     */
    public void addEdge(@NonNull Edge edge) {
        addNode(edge.getSource());
        addNode(edge.getDestination());

        if (!this.edges.contains(edge)) {
            this.edges.add(edge);

            for (NodeObserver observer : observers) {
                observer.notifyInvalidated();
            }
        }
    }


    /**
     * Add one or more {@link Edge Edges} to the graph.
     *
     * @param edges
     */
    public void addEdges(@NonNull Edge... edges) {
        for (int i = 0; i < edges.length; i++) {
            addEdge(edges[i]);
        }
    }


    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    /**
     * Remove {@link Edge Edges} from the graph.
     *
     * @param edges
     */
    public void removeEdges(@NonNull Edge... edges) {
        for (int i = 0; i < edges.length; i++) {
            removeEdge(edges[i]);
        }
    }

    public void removeEdge(Node predecessor, Node current) {

        final Iterator<Edge> iterator = getEdges().iterator();
        while (iterator.hasNext()) {
            Edge edge = iterator.next();
            if (edge.getSource().equals(predecessor) && edge.getDestination().equals(current)) {
                iterator.remove();
            }
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

    public Edge getEdge(Node source, Node destination) {

        for (Edge edge : getEdges()) {
            if (edge.getSource().equals(source) && edge.getDestination().equals(destination)) {
                return edge;
            }
        }
        return null;
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

    public List<Edge> getOutEdges(final Node node) {
        List<Edge> outEdges = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getSource().equals(node)) {
                outEdges.add(edge);
            }
        }
        return outEdges;
    }

    private List<Edge> getInEdges(final Node node) {
        ArrayList<Edge> inEdges = new ArrayList<>();
        for (final Edge edge : edges) {
            if (edge.getDestination().equals(node)) {
                inEdges.add(edge);
            }
        }
        return inEdges;
    }

    // Todo this is a quick fix and should be removed later
    public void setAsTree(boolean isTree) {
        this.isTree = isTree;
    }
}
