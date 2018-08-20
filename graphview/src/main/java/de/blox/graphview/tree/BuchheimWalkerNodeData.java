package de.blox.graphview.tree;

import de.blox.graphview.Node;

/**
 * Class to save additional data used by the buchheim-walker algorithm.
 */
class BuchheimWalkerNodeData {
    private Node mAncestor;
    private Node mThread;
    private int mNumber;
    private int mDepth;
    private double mPrelim;
    private double mModifier;
    private double mShift;
    private double mChange;

    Node getAncestor() {
        return mAncestor;
    }

    void setAncestor(Node ancestor) {
        mAncestor = ancestor;
    }

    Node getThread() {
        return mThread;
    }

    void setThread(Node thread) {
        mThread = thread;
    }

    int getNumber() {
        return mNumber;
    }

    void setNumber(int number) {
        mNumber = number;
    }

    int getDepth() {
        return mDepth;
    }

    void setDepth(int depth) {
        mDepth = depth;
    }

    double getPrelim() {
        return mPrelim;
    }

    void setPrelim(double prelim) {
        mPrelim = prelim;
    }

    double getModifier() {
        return mModifier;
    }

    void setModifier(double modifier) {
        mModifier = modifier;
    }

    double getShift() {
        return mShift;
    }

    void setShift(double shift) {
        mShift = shift;
    }

    double getChange() {
        return mChange;
    }

    void setChange(double change) {
        mChange = change;
    }
}
