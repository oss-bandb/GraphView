package de.blox.graphview.layered;

/**
 * Class to save additional data used by the sugiyama algorithm.
 */
class SugiyamaNodeData {
    boolean reversed = false;
    boolean dummy = false;
    int median = -1;
    int layer = -1;

    public boolean isDummy() {
        return dummy;
    }

    public boolean isReversed() {
        return reversed;
    }

    @Override
    public String toString() {
        return "SugiyamaNodeData{" +
                ", reversed=" + reversed +
                ", dummy=" + dummy +
                ", median=" + median +
                ", layer=" + layer +
                '}';
    }
}
