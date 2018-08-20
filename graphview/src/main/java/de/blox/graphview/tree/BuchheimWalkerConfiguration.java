package de.blox.graphview.tree;

/**
 */

public class BuchheimWalkerConfiguration {
    public static final int DEFAULT_SIBLING_SEPARATION = 100;
    public static final int DEFAULT_SUBTREE_SEPARATION = DEFAULT_SIBLING_SEPARATION;
    public static final int DEFAULT_LEVEL_SEPARATION = 100;

    private final int siblingSeparation;
    private final int levelSeparation;
    private final int subtreeSeparation;

    public BuchheimWalkerConfiguration() {
        this(DEFAULT_SIBLING_SEPARATION, DEFAULT_LEVEL_SEPARATION, DEFAULT_SUBTREE_SEPARATION);
    }

    public BuchheimWalkerConfiguration(int siblingSeparation, int levelSeparation, int subtreeSeparation) {
        this.siblingSeparation = siblingSeparation;
        this.levelSeparation = levelSeparation;
        this.subtreeSeparation = subtreeSeparation;
    }

    public int getSiblingSeparation() {
        return siblingSeparation;
    }

    public int getLevelSeparation() {
        return levelSeparation;
    }

    public int getSubtreeSeparation() {
        return subtreeSeparation;
    }
}
