package de.blox.graphview.tree;

/**
 */

public class BuchheimWalkerConfiguration {
    public static final int ORIENTATION_TOP_BOTTOM = 1;
    public static final int ORIENTATION_BOTTOM_TOP = 2;
    public static final int ORIENTATION_LEFT_RIGHT = 3;
    public static final int ORIENTATION_RIGHT_LEFT = 4;

    public static final int DEFAULT_SIBLING_SEPARATION = 100;
    public static final int DEFAULT_SUBTREE_SEPARATION = 100;
    public static final int DEFAULT_LEVEL_SEPARATION = 100;
    public static final int DEFAULT_ORIENTATION = ORIENTATION_TOP_BOTTOM;

    private final int siblingSeparation;
    private final int levelSeparation;
    private final int subtreeSeparation;
    private final int orientation;


    private BuchheimWalkerConfiguration(Builder builder) {
        this.siblingSeparation = builder.siblingSeparation;
        this.levelSeparation = builder.levelSeparation;
        this.subtreeSeparation = builder.subtreeSeparation;
        this.orientation = builder.orientation;
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

    public int getOrientation() {
        return orientation;
    }

    public static class Builder {
        private int siblingSeparation = DEFAULT_SIBLING_SEPARATION;
        private int levelSeparation = DEFAULT_LEVEL_SEPARATION;
        private int subtreeSeparation = DEFAULT_SUBTREE_SEPARATION;
        private int orientation = DEFAULT_ORIENTATION;

        public Builder setSiblingSeparation(int siblingSeparation) {
            this.siblingSeparation = siblingSeparation;
            return this;
        }

        public Builder setLevelSeparation(int levelSeparation) {
            this.levelSeparation = levelSeparation;
            return this;
        }

        public Builder setSubtreeSeparation(int subtreeSeparation) {
            this.subtreeSeparation = subtreeSeparation;
            return this;
        }

        public Builder setOrientation(int orientation) {
            this.orientation = orientation;
            return this;
        }

        public BuchheimWalkerConfiguration build() {
            return new BuchheimWalkerConfiguration(this);
        }
    }
}
