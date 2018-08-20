package de.blox.graphview;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Adapter;

import de.blox.graphview.tree.BuchheimWalkerAlgorithm;

/**
 *
 */
public interface GraphAdapter<VH> extends Adapter, NodeObserver {

    void notifySizeChanged();

    /**
     * Returns the currently set algorithm. It uses the {@link BuchheimWalkerAlgorithm} as default,
     * if no algorithm is previously set.
     *
     * @return
     */
    Algorithm getAlgorithm();

    /**
     * Set an algorithm, which is used for laying out the tree.
     *
     * @param algorithm the algorithm to use for laying out the tree
     */
    void setAlgorithm(@NonNull Algorithm algorithm);


    /**
     * Set a graph.
     * @param graph
     */
    void setGraph(@NonNull Graph graph);

    /**
     * Returns the node at a given {code position}.
     *
     * @param position
     * @return
     */
    Node getNode(int position);

    /**
     * Returns the screen position from the node at {code position}
     *
     * @param position
     * @return
     */
    Vector getScreenPosition(int position);

    @NonNull
    VH onCreateViewHolder(View view);

    void onBindViewHolder(VH viewHolder, Object data, int position);

    Graph getGraph();
}
