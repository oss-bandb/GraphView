package de.blox.treeview;

/**
 *
 */

class TreeNodeSize {

    private int mWidth;
    private int mHeight;

    public TreeNodeSize() {}

    public TreeNodeSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void set(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

}
