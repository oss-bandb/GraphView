package de.blox.graphview;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import de.blox.graphview.tree.BuchheimWalkerAlgorithm;

public abstract class BaseGraphAdapter<VH extends ViewHolder> implements GraphAdapter<VH> {
    private Graph graph;

    private Algorithm mAlgorithm;

    private DataSetObservable mDataSetObservable = new DataSetObservable();

    public BaseGraphAdapter() {

    }

    public BaseGraphAdapter(@NonNull Graph graph) {
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
            viewHolder = onCreateViewHolder(parent, getItemViewType(position));
            view = viewHolder.itemView;
            view.setTag(viewHolder);
        } else {
            viewHolder = (VH) convertView.getTag();
            view = viewHolder.itemView;
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
