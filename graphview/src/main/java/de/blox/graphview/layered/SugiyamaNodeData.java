package de.blox.graphview.layered;

import java.util.HashSet;
import java.util.Set;

import de.blox.graphview.Node;

/**
 * Class to save additional data used by the sugiyama algorithm.
 */
class SugiyamaNodeData {
    Set<Node> reversed = new HashSet<>();
    boolean dummy = false;
    int median = -1;
    int layer = -1;

    public boolean isDummy() {
        return dummy;
    }

    public boolean isReversed() {
        return !reversed.isEmpty();
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
