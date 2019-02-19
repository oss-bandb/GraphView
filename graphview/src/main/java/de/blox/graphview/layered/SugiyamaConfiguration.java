package de.blox.graphview.layered;

/**
 *
 */
public class SugiyamaConfiguration {

    public static final int X_SEPARATION = 100;
    public static final int Y_SEPARATION = 100;

    private final int levelSeparation;
    private final int nodeSeparation;

    public int getLevelSeparation() {
        return levelSeparation;
    }

    public int getNodeSeparation() {
        return nodeSeparation;
    }

    public SugiyamaConfiguration(Builder builder) {
        levelSeparation = builder.levelSeparation;
        nodeSeparation = builder.nodeSeparation;
    }

    //TODO: enhance builder-pattern
    public static class Builder {

        private int levelSeparation = Y_SEPARATION;

        private int nodeSeparation = X_SEPARATION;

        public Builder setLevelSeparation(int levelSeparation) {
            this.levelSeparation = levelSeparation;
            return this;
        }

        public Builder setNodeSeparation(int nodeSeparation) {
            this.nodeSeparation = nodeSeparation;
            return this;
        }
    }
}
