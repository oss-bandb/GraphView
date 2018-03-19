package de.blox.treeview;

/**
 *
 */

public class AlgorithmFactory {

    public static Algorithm createBuchheimWalker(BuchheimWalkerConfiguration configuration) {
        return new BuchheimWalkerAlgorithm(configuration);
    }

    public static Algorithm createDefaultBuchheimWalker() {
        return new BuchheimWalkerAlgorithm();
    }
}
