package de.blox.treeview;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Adapter;

/**
 *
 */
interface TreeAdapter<VH> extends Adapter, TreeNodeObserver {

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
     * Set a new root node. This triggers the re-drawing of the whole view.
     *
     * @param rootNode
     */
    void setRootNode(@NonNull TreeNode rootNode);

    /**
     * Returns the node at a given {code position}.
     *
     * @param position
     * @return
     */
    TreeNode getNode(int position);

    /**
     * Returns the screen position from the node at {code position}
     *
     * @param position
     * @return
     */
    Point getScreenPosition(int position);

    @NonNull
    VH onCreateViewHolder(View view);

    void onBindViewHolder(VH viewHolder, Object data, int position);
}
