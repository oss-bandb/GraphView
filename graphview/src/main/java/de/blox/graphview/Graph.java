package de.blox.graphview;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class Graph {
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    private List<NodeObserver> observers = new ArrayList<>();

    public void removeNode(@NonNull Node node) {
        if(!nodes.contains(node)) {
            throw new IllegalArgumentException("Unable to find node in graph.");
        }

        nodes.remove(node);

        final Iterator<Edge> iterator = edges.iterator();
        while (iterator.hasNext()) {
            Edge edge = iterator.next();
            if(edge.getSource().equals(node) || edge.getDestination().equals(node)) {
                iterator.remove();
            }
        }

        for(NodeObserver observer : observers) {
            observer.notifyNodeRemoved(node);
        }
    }

    public void addEdge(Node source, Node destination) {
        if(!nodes.contains(source)) {
            nodes.add(source);
        }

        if(!nodes.contains(destination)) {
            nodes.add(destination);
        }

        edges.add(new Edge(source, destination));

        for(NodeObserver observer : observers) {
            observer.notifyInvalidated();
        }
    }

    public void addNodeObserver(NodeObserver nodeObserver) {
        observers.add(nodeObserver);
    }

    public void removeNodeObserver(NodeObserver nodeObserver) {
        observers.remove(nodeObserver);
    }

    public Node getNode(int position) {
        if(position < 0) {
            throw new IllegalArgumentException("position can't be negative");
        }

        final int size = nodes.size();
        if(position >= size) {
            throw new IndexOutOfBoundsException("Position: " + position + ", Size: " + size);
        }
        
        return nodes.get(position);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public boolean hasSuccessor(Node node) {
        for(Edge edge : edges) {
            if(edge.getSource().equals(node)) {
                return true;
            }
        }

        return false;
    }

    public List<Node> successorsOf(Node node) {
        List<Node> successors = new ArrayList<>();
        for(Edge edge : edges) {
            if(edge.getSource().equals(node)) {
                successors.add(edge.getDestination());
            }
        }

        return successors;
    }

    public List<Node> predecessorsOf(Node node) {
        List<Node> predecessors = new ArrayList<>();
        for(Edge edge : edges) {
            if(edge.getDestination().equals(node)) {
                predecessors.add(edge.getSource());
            }
        }

        return predecessors;
    }
}
