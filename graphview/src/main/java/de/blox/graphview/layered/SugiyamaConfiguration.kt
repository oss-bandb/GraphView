package de.blox.graphview.layered

class SugiyamaConfiguration private constructor(builder: Builder) {
    val levelSeparation: Int = builder.levelSeparation
    val nodeSeparation: Int = builder.nodeSeparation

    class Builder {
        var levelSeparation = Y_SEPARATION
            private set
        var nodeSeparation = X_SEPARATION
            private set

        fun setLevelSeparation(levelSeparation: Int) = apply {
            this.levelSeparation = levelSeparation
        }

        fun setNodeSeparation(nodeSeparation: Int) = apply {
            this.nodeSeparation = nodeSeparation
        }

        fun build() = SugiyamaConfiguration(this)
    }

    companion object {
        const val X_SEPARATION = 100
        const val Y_SEPARATION = 100
    }
}
