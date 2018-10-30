package de.blox.graphview;

public class Vector {
    private final float x;
    private final float y;

    public Vector() {
        this(0, 0);
    }

    public Vector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector add(Vector operand) {
        return new Vector(operand.x + x, operand.y + y);
    }

    public Vector add(float x, float y) {
        return new Vector(this.x + x, this.y + y);
    }

    public Vector subtract(Vector operand) {
        return new Vector(x - operand.x, y - operand.y);
    }

    public Vector subtract(float x, float y) {
        return new Vector(this.x - x, this.y - y);
    }

    public Vector multiply(Vector operand) {
        return new Vector(x * operand.x, y * operand.y);
    }

    public Vector multiply(float operand) {
        return new Vector(x * operand, y * operand);
    }

    public Vector divide(Vector operand) {
        return new Vector(x / operand.x, y / operand.y);
    }

    public Vector divide(float operand) {
        return new Vector(x / operand, y / operand);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}