package de.blox.treeview;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */

public class TreeNode {
    private Object mData;
    private int mX;
    private int mY;
    private int mLevel;
    private int mNodeCount = 1;
    private TreeNode mParent;
    private List<TreeNode> mChildren = new ArrayList<>();
    private List<TreeNodeObserver> mTreeNodeObservers = new ArrayList<>();

    public TreeNode() {
        this(null);
    }

    public TreeNode(Object data) {
        mData = data;
    }

    int getLevel() {
        return mLevel;
    }

    void setLevel(int level) {
        mLevel = level;
    }

    int getX() {
        return mX;
    }

    void setX(int x) {
        mX = x;
    }

    int getY() {
        return mY;
    }

    void setY(int y) {
        mY = y;
    }

    public Object getData() {
        return mData;
    }

    public void setData(Object data) {
        mData = data;

        for (TreeNodeObserver observer : getTreeNodeObservers()) {
            observer.notifyDataChanged(this);
        }
    }

    public boolean hasData() {
        return mData != null;
    }

    private void notifyParentNodeCountChanged() {
        if (mParent != null) {
            mParent.notifyParentNodeCountChanged();
        } else {
            calculateNodeCount();
        }
    }

    private int calculateNodeCount() {
        int size = 1;

        for (TreeNode child : mChildren) {
            size += child.calculateNodeCount();
        }

        return mNodeCount = size;
    }

    @Nullable
    TreeNode getNodeAtPosition(int position) {
        if (position == 0) {
            return this;
        } else {
            int childPosition = position - 1;
            int count;
            for (TreeNode child : getChildren()) {
                count = child.getNodeCount();
                if (childPosition < count) {
                    return child.getNodeAtPosition(childPosition);
                } else {
                    childPosition -= count;
                }
            }
        }

        // TODO no node at this position. throw exception?
        return null;
    }

    public void addChild(TreeNode child) {
        mChildren.add(child);
        child.setParent(this);

        notifyParentNodeCountChanged();

        for (TreeNodeObserver observer : getTreeNodeObservers()) {
            observer.notifyNodeAdded(child, this);
        }
    }

    public void addChildren(TreeNode... children) {
        addChildren(Arrays.asList(children));
    }

    public void addChildren(List<TreeNode> children) {
        for (TreeNode child : children) {
            addChild(child);
        }
    }

    public void removeChild(TreeNode child) {
        child.setParent(null);
        mChildren.remove(child);

        notifyParentNodeCountChanged();

        for (TreeNodeObserver observer : getTreeNodeObservers()) {
            observer.notifyNodeRemoved(child, this);
        }
    }

    public int getNodeCount() {
        return mNodeCount;
    }

    public TreeNode getParent() {
        return mParent;
    }

    public void setParent(TreeNode parent) {
        mParent = parent;
    }

    public List<TreeNode> getChildren() {
        return mChildren;
    }

    public boolean hasChildren() {
        return !mChildren.isEmpty();
    }

    public boolean hasParent() {
        return mParent != null;
    }

    void addTreeNodeObserver(TreeNodeObserver observer) {
        mTreeNodeObservers.add(observer);
    }

    void removeTreeNodeObserver(TreeNodeObserver observer) {
        mTreeNodeObservers.remove(observer);
    }

    private List<TreeNodeObserver> getTreeNodeObservers() {
        List<TreeNodeObserver> observers = mTreeNodeObservers;
        if (observers.isEmpty() && mParent != null) {
            observers = mParent.getTreeNodeObservers();
        }
        return observers;
    }

    @Override
    public String toString() {
        String indent = "\t";
        for (int i = 0; i < (mY / 10); i++) {
            indent += indent;
        }
        return "\n" + indent + "TreeNode{" +
                " data=" + mData +
                ", mX=" + mX +
                ", mY=" + mY +
                ", mChildren=" + mChildren +
                '}';
    }
}
