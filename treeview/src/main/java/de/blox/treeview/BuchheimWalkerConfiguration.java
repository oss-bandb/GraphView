package de.blox.treeview;

/**
 */

public class BuchheimWalkerConfiguration {

    private final int mSiblingSeparation;
    private final int mSubtreeSeparation;

    public BuchheimWalkerConfiguration(int siblingSeparation, int subtreeSeparation) {

        mSiblingSeparation = siblingSeparation;
        mSubtreeSeparation = subtreeSeparation;
    }

    public int getSiblingSeparation() {
        return mSiblingSeparation;
    }

    public int getSubtreeSeparation() {
        return mSubtreeSeparation;
    }
}
