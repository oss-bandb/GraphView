package de.blox.graphview;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.blox.graphview.tree.BuchheimWalkerAlgorithm;

/**
 *
 */

public abstract class BaseGraphAdapter<VH> implements GraphAdapter<VH> {
    private final int mLayoutRes;
    private Graph graph;

    private Algorithm mAlgorithm;

    private LayoutInflater mLayoutInflater;

    private DataSetObservable mDataSetObservable = new DataSetObservable();

    public BaseGraphAdapter(@NonNull Context context, @LayoutRes int layoutRes) {
        mLayoutInflater = LayoutInflater.from(context);
        mLayoutRes = layoutRes;
    }

    public BaseGraphAdapter(@NonNull Context context, @LayoutRes int layoutRes, @NonNull Graph graph) {
        this(context, layoutRes);
        setGraph(graph);
    }

    @Override
    public void notifySizeChanged() {
        if (graph != null && graph.getNodeCount() > 0) {
            getAlgorithm().run(graph);
        }
    }

    @Override
    public Algorithm getAlgorithm() {
        if (mAlgorithm == null) {
            mAlgorithm = new BuchheimWalkerAlgorithm();
        }

        return mAlgorithm;
    }

    @Override
    public void setAlgorithm(@NonNull Algorithm algorithm) {
        Conditions.isNonNull(algorithm, "algorithm can't be null");

        mAlgorithm = algorithm;
        if(graph != null) {
            graph.setAsTree(getAlgorithm() instanceof BuchheimWalkerAlgorithm);
        }
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public void setGraph(@NonNull Graph graph) {
        Conditions.isNonNull(graph, "graph can't be null");

        if (this.graph != null) {
            this.graph.removeNodeObserver(this);
        }

        this.graph = graph;
        this.graph.addNodeObserver(this);

        mDataSetObservable.notifyChanged();

        graph.setAsTree(getAlgorithm() instanceof BuchheimWalkerAlgorithm);
    }

    @Override
    public Node getNode(int position) {
        return graph != null ? graph.getNode(position) : null;
    }

    @Override
    public Vector getScreenPosition(int position) {
        return getNode(position).getPosition();
    }

    @Override
    public int getCount() {
        return graph != null ? graph.getNodeCount() : 0;
    }

    @Override
    public void notifyDataChanged(Node node) {
        mDataSetObservable.notifyChanged();
    }

    @Override
    public void notifyNodeAdded(Node node) {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public void notifyNodeRemoved(Node node) {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public void notifyInvalidated() {
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

        Node node = getNode(position);
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
