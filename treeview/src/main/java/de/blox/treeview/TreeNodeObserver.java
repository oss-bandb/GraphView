package de.blox.treeview;

/**
 *
 */

interface TreeNodeObserver {
    void notifyDataChanged(TreeNode node);
    void notifyNodeAdded(TreeNode node, TreeNode parent);
    void notifyNodeRemoved(TreeNode node, TreeNode parent);
}
