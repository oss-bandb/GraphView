package de.blox.graphview.tree

class BuchheimWalkerConfiguration private constructor(builder: Builder) {
    val siblingSeparation: Int
    val levelSeparation: Int
    val subtreeSeparation: Int
    val orientation: Int

    init {
        this.siblingSeparation = builder.siblingSeparation
        this.levelSeparation = builder.levelSeparation
        this.subtreeSeparation = builder.subtreeSeparation
        this.orientation = builder.orientation
    }

    class Builder {
        var siblingSeparation = DEFAULT_SIBLING_SEPARATION
            private set
        var levelSeparation = DEFAULT_LEVEL_SEPARATION
            private set
        var subtreeSeparation = DEFAULT_SUBTREE_SEPARATION
            private set
        var orientation = DEFAULT_ORIENTATION
            private set

        fun setSiblingSeparation(siblingSeparation: Int) = apply {
            this.siblingSeparation = siblingSeparation
        }

        fun setLevelSeparation(levelSeparation: Int) = apply {
            this.levelSeparation = levelSeparation
        }

        fun setSubtreeSeparation(subtreeSeparation: Int) = apply {
            this.subtreeSeparation = subtreeSeparation
        }

        fun setOrientation(orientation: Int) = apply {
            this.orientation = orientation
        }

        fun build() = BuchheimWalkerConfiguration(this)
    }

    companion object {
        // TODO: refactor to sealed class
        const val ORIENTATION_TOP_BOTTOM = 1
        const val ORIENTATION_BOTTOM_TOP = 2
        const val ORIENTATION_LEFT_RIGHT = 3
        const val ORIENTATION_RIGHT_LEFT = 4

        const val DEFAULT_SIBLING_SEPARATION = 100
        const val DEFAULT_SUBTREE_SEPARATION = 100
        const val DEFAULT_LEVEL_SEPARATION = 100
        const val DEFAULT_ORIENTATION = ORIENTATION_TOP_BOTTOM
    }
}
