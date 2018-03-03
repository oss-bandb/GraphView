package de.blox.treeview;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public abstract class BaseTreeAdapter<VH> implements TreeAdapter<VH> {
    private final int mLayoutRes;
    private TreeNode mRootNode;

    private Algorithm mAlgorithm;

    private LayoutInflater mLayoutInflater;

    private DataSetObservable mDataSetObservable = new DataSetObservable();

    public BaseTreeAdapter(@NonNull Context context, @LayoutRes int layoutRes) {
        mLayoutInflater = LayoutInflater.from(context);
        mLayoutRes = layoutRes;
    }

    @Override
    public void notifySizeChanged() {
        if (mRootNode != null) {
            getAlgorithm().run(mRootNode);
        }
    }

    @Override
    public Algorithm getAlgorithm() {
        if (mAlgorithm == null) {
            mAlgorithm = AlgorithmFactory.createDefaultBuchheimWalker();
        }

        return mAlgorithm;
    }

    @Override
    public void setAlgorithm(@NonNull Algorithm algorithm) {
        Conditions.isNonNull(algorithm, "algorithm can't be null");

        mAlgorithm = algorithm;
    }

    @Override
    public void setRootNode(@NonNull TreeNode rootNode) {
        Conditions.isNonNull(rootNode, "rootNode can't be null");

        if (mRootNode != null) {
            mRootNode.removeTreeNodeObserver(this);
        }

        mRootNode = rootNode;
        mRootNode.addTreeNodeObserver(this);
        notifyDataChanged(mRootNode);
    }

    @Override
    public TreeNode getNode(int position) {
        List<TreeNode> list = new ArrayList<>();
        list.add(mRootNode);

        return mRootNode != null ? breadthSearch(list, position) : null;
    }

    protected TreeNode breadthSearch(List<TreeNode> nodes, int position) {
        if (nodes.size() > position) {
            return nodes.get(position);
        }

        List<TreeNode> childNodes = new ArrayList<>();
        for (TreeNode n : nodes) {
            for (TreeNode child : n.getChildren())
                childNodes.add(child);
        }

        return breadthSearch(childNodes, position - nodes.size());
    }

    @Override
    public Point getScreenPosition(int position) {
        TreeNode node = getNode(position);

        return new Point(node.getX(), node.getY());
    }

    @Override
    public int getCount() {
        return mRootNode != null ? mRootNode.getNodeCount() : 0;
    }

    @Override
    public void notifyDataChanged(TreeNode node) {
        mDataSetObservable.notifyChanged();
    }

    @Override
    public void notifyNodeAdded(TreeNode node, TreeNode parent) {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public void notifyNodeRemoved(TreeNode node, TreeNode parent) {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    @Override
    public Object getItem(int position) {
        return getNode(position).getData();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        VH viewHolder;

        if (convertView == null) {
            view = mLayoutInflater.inflate(mLayoutRes, parent, false);
            viewHolder = onCreateViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (VH) view.getTag();
        }

        TreeNode node = getNode(position);
        onBindViewHolder(viewHolder, node.getData(), position);

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
