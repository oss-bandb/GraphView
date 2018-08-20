package de.blox.graphview;


/**
 *
 */
public class Node {
    private Vector pos;
    private Object data;
    private Size size;

    public Node(Object data) {
        this.data = data;
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

    @Override
    public String toString() {
        return "Node{" +
                "pos=" + pos +
                ", data=" + data +
                ", size=" + size +
                '}';
    }
}
