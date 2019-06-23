package de.blox.graphview;


import java.util.IdentityHashMap;
import java.util.Map;

/**
 *
 */
public class Node {
    private Vector pos;
    private Object data;
    private Size size;
    private Map<Graph, Integer> graphCount = new IdentityHashMap<>();

    public Node(Object data) {
        this.data = data;
        setPos(new Vector());
        size = new Size(0, 0);
    }

    public Vector getPosition() {
        return pos;
    }

    public void setPos(Vector pos) {
        this.pos = pos;
    }

    public float getX() {
        return pos.getX();
    }

    public float getY() {
        return pos.getY();
    }

    public void setX(float x) {
        this.pos.setX(x);
    }

    public void setY(float y) {
        this.pos.setY(y);
    }

    public void replaceData(Object data) {
        this.data = data;
        for (Graph graph : graphCount.keySet()) {
            graph.onNodeDataReplaced(this);
        }
    }

    public Object getData() {
        return data;
    }

    public void setSize(int width, int height) {
        size = new Size(width, height);
    }

    public int getWidth() {
        return size.getWidth();
    }

    public int getHeight() {
        return size.getHeight();
    }

    void onAddedToGraph(Graph graph) {
        Integer count = graphCount.get(graph);
        if (count == null)
            count = 1;
        graphCount.put(graph, count + 1);
    }

    void onRemovedFromGraph(Graph graph) {
        Integer count = graphCount.get(graph);
        if (count == null)
            throw new IllegalStateException("Node shouldn't be removed from a graph not associated with");
        if (count == 1) {
            graphCount.remove(graph);
        } else {
            graphCount.put(graph, count -1 );
        }
    }

    @Override
    public String toString() {
        return "Node{" +
                "pos=" + pos +
                ", data=" + data +
                ", size=" + size +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return data.equals(node.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
