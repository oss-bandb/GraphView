package de.blox.treeview;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BuchheimWalkerAlgorithm implements Algorithm {

    private static final int DEFAULT_SIBLING_SEPARATION = 100;
    private static final int DEFAULT_SUBTREE_SEPARATION = 100;

    private BuchheimWalkerConfiguration mConfiguration;
    private Map<TreeNode, BuchheimWalkerNodeData> mNodeData = new HashMap<>();

    BuchheimWalkerAlgorithm(BuchheimWalkerConfiguration configuration) {
        mConfiguration = configuration;
    }

    BuchheimWalkerAlgorithm() {
        this(new BuchheimWalkerConfiguration(DEFAULT_SIBLING_SEPARATION, DEFAULT_SUBTREE_SEPARATION));
    }

    private BuchheimWalkerNodeData createNodeData(TreeNode node) {
        BuchheimWalkerNodeData nodeData = new BuchheimWalkerNodeData();
        nodeData.setAncestor(node);
        mNodeData.put(node, nodeData);

        return nodeData;
    }

    private BuchheimWalkerNodeData getNodeData(TreeNode node) {
        return mNodeData.get(node);
    }

    private void firstWalk(TreeNode node, int depth, int number) {
        BuchheimWalkerNodeData nodeData = createNodeData(node);
        nodeData.setDepth(depth);
        nodeData.setNumber(number);

        if (isLeaf(node)) {
            // if the node has no left sibling, prelim(node) should be set to 0, but we don't have to set it
            // here, because it's already initialized with 0
            if (hasLeftSibling(node)) {
                TreeNode leftSibling = getLeftSibling(node);
                nodeData.setPrelim(getPrelim(leftSibling) + getSpacing(leftSibling, node));
            }
        } else {
            TreeNode leftMost = getLeftMostChild(node);
            TreeNode rightMost = getRightMostChild(node);
            TreeNode defaultAncestor = leftMost;

            TreeNode next = leftMost;
            int i = 1;
            while (next != null) {
                firstWalk(next, depth + 1, i++);
                defaultAncestor = apportion(next, defaultAncestor);

                next = getRightSibling(next);
            }

            executeShifts(node);

            double midPoint = 0.5 * ((getPrelim(leftMost) + getPrelim(rightMost) + rightMost.getWidth()) - node.getWidth());

            if (hasLeftSibling(node)) {
                TreeNode leftSibling = getLeftSibling(node);
                nodeData.setPrelim(getPrelim(leftSibling) + getSpacing(leftSibling, node));
                nodeData.setModifier(nodeData.getPrelim() - midPoint);
            } else {
                nodeData.setPrelim(midPoint);
            }
        }
    }

    private void secondWalk(TreeNode node, double modifier) {
        BuchheimWalkerNodeData nodeData = getNodeData(node);
        node.setX((int) (nodeData.getPrelim() + modifier));
        node.setY(nodeData.getDepth());
        node.setLevel(nodeData.getDepth());

        for (TreeNode w : node.getChildren()) {
            secondWalk(w, modifier + nodeData.getModifier());
        }
    }

    private void executeShifts(TreeNode node) {
        double shift = 0, change = 0;
        TreeNode w = getRightMostChild(node);
        while (w != null) {
            BuchheimWalkerNodeData nodeData = getNodeData(w);

            nodeData.setPrelim(nodeData.getPrelim() + shift);
            nodeData.setModifier(nodeData.getModifier() + shift);
            change += nodeData.getChange();
            shift += nodeData.getShift() + change;

            w = getLeftSibling(w);
        }
    }

    private TreeNode apportion(TreeNode node, TreeNode defaultAncestor) {
        if (hasLeftSibling(node)) {
            TreeNode leftSibling = getLeftSibling(node);

            TreeNode vip = node;
            TreeNode vop = node;
            TreeNode vim = leftSibling;
            TreeNode vom = getLeftMostChild(vip.getParent());

            double sip = getModifier(vip);
            double sop = getModifier(vop);
            double sim = getModifier(vim);
            double som = getModifier(vom);

            TreeNode nextRight = nextRight(vim);
            TreeNode nextLeft = nextLeft(vip);

            while (nextRight != null && nextLeft != null) {
                vim = nextRight;
                vip = nextLeft;
                vom = nextLeft(vom);
                vop = nextRight(vop);

                setAncestor(vop, node);

                double shift = (getPrelim(vim) + sim) - (getPrelim(vip) + sip) + getSpacing(vim, vip);
                if (shift > 0) {
                    moveSubtree(ancestor(vim, node, defaultAncestor), node, shift);
                    sip += shift;
                    sop += shift;
                }

                sim += getModifier(vim);
                sip += getModifier(vip);
                som += getModifier(vom);
                sop += getModifier(vop);

                nextRight = nextRight(vim);
                nextLeft = nextLeft(vip);
            }

            if (nextRight != null && nextRight(vop) == null) {
                setThread(vop, nextRight);
                setModifier(vop, getModifier(vop) + sim - sop);
            }

            if (nextLeft != null && nextLeft(vom) == null) {
                setThread(vom, nextLeft);
                setModifier(vom, getModifier(vom) + sip - som);
                defaultAncestor = node;
            }
        }

        return defaultAncestor;
    }

    private void setAncestor(TreeNode v, TreeNode ancestor) {
        getNodeData(v).setAncestor(ancestor);
    }

    private void setModifier(TreeNode v, double modifier) {
        getNodeData(v).setModifier(modifier);
    }

    private void setThread(TreeNode v, TreeNode thread) {
        getNodeData(v).setThread(thread);
    }

    private double getPrelim(TreeNode v) {
        return getNodeData(v).getPrelim();
    }

    private double getModifier(TreeNode vip) {
        return getNodeData(vip).getModifier();
    }

    private void moveSubtree(TreeNode wm, TreeNode wp, double shift) {
        BuchheimWalkerNodeData wpNodeData = getNodeData(wp);
        BuchheimWalkerNodeData wmNodeData = getNodeData(wm);

        int subtrees = wpNodeData.getNumber() - wmNodeData.getNumber();
        wpNodeData.setChange(wpNodeData.getChange() - shift / subtrees);
        wpNodeData.setShift(wpNodeData.getShift() + shift);
        wmNodeData.setChange(wmNodeData.getChange() + shift / subtrees);
        wpNodeData.setPrelim(wpNodeData.getPrelim() + shift);
        wpNodeData.setModifier(wpNodeData.getModifier() + shift);
    }

    private TreeNode ancestor(TreeNode vim, TreeNode node, TreeNode defaultAncestor) {
        BuchheimWalkerNodeData vipNodeData = getNodeData(vim);

        if (vipNodeData.getAncestor().getParent() == node.getParent()) {
            return vipNodeData.getAncestor();
        }

        return defaultAncestor;
    }

    private TreeNode nextRight(TreeNode node) {
        if (node.hasChildren()) {
            return getRightMostChild(node);
        }

        return getNodeData(node).getThread();
    }

    private TreeNode nextLeft(TreeNode node) {
        if (node.hasChildren()) {
            return getLeftMostChild(node);
        }

        return getNodeData(node).getThread();
    }

    private int getSpacing(TreeNode leftNode, TreeNode rightNode) {
        return mConfiguration.getSiblingSeparation() + leftNode.getWidth();
    }

    private boolean isLeaf(TreeNode node) {
        return node.getChildren().isEmpty();
    }

    private TreeNode getLeftSibling(TreeNode node) {
        if (!hasLeftSibling(node)) {
            return null;
        }

        TreeNode parent = node.getParent();
        List<TreeNode> children = parent.getChildren();
        int nodeIndex = children.indexOf(node);
        return children.get(nodeIndex - 1);
    }

    private boolean hasLeftSibling(TreeNode node) {
        TreeNode parent = node.getParent();
        if (parent == null) {
            return false;
        }

        int nodeIndex = parent.getChildren().indexOf(node);
        return nodeIndex > 0;
    }

    private TreeNode getRightSibling(TreeNode node) {
        if (!hasRightSibling(node)) {
            return null;
        }

        TreeNode parent = node.getParent();
        List<TreeNode> children = parent.getChildren();
        int nodeIndex = children.indexOf(node);
        return children.get(nodeIndex + 1);
    }

    private boolean hasRightSibling(TreeNode node) {
        TreeNode parent = node.getParent();
        if (parent == null) {
            return false;
        }

        List<TreeNode> children = parent.getChildren();
        int nodeIndex = children.indexOf(node);
        return nodeIndex < children.size() - 1;
    }

    private TreeNode getLeftMostChild(TreeNode node) {
        return node.getChildren().get(0);
    }

    private TreeNode getRightMostChild(TreeNode node) {
        List<TreeNode> children = node.getChildren();
        if (children.isEmpty()) {
            return null;
        }

        return children.get(children.size() - 1);
    }

    @Override
    public void run(TreeNode root) {
        mNodeData.clear();

        firstWalk(root, 0, 0);
        secondWalk(root, -getPrelim(root));
    }
}
