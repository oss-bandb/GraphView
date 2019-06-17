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

    public void setX(float x) {
        this.pos.setX(x);
    }

    public float getY() {
        return pos.getY();
    }

    public void setY(float y) {
        this.pos.setY(y);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
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
