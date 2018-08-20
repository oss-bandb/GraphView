package de.blox.graphview;

public class Vector {
    private final double x;
    private final double y;

    public Vector() {
        this(0, 0);
    }

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector add(Vector operand) {
        return new Vector(operand.x + x, operand.y + y);
    }

    public Vector subtract(Vector operand) {
        return new Vector(x - operand.x, y - operand.y);
    }

    public Vector multiply(Vector operand) {
        return new Vector(x * operand.x, y * operand.y);
    }

    public Vector multiply(double operand) {
        return new Vector(x * operand, y * operand);
    }

    public Vector divide(Vector operand) {
        return new Vector(x / operand.x, y / operand.y);
    }

    public Vector divide(double operand) {
        return new Vector(x / operand, y / operand);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
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