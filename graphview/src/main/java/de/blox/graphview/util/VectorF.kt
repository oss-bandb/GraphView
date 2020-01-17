package de.blox.graphview.util

import kotlin.math.sqrt

data class VectorF constructor(var x: Float = 0f, var y: Float = 0f) {

    fun add(operand: VectorF): VectorF {
        return VectorF(operand.x + x, operand.y + y)
    }

    fun add(x: Float, y: Float): VectorF {
        return VectorF(this.x + x, this.y + y)
    }

    fun subtract(operand: VectorF): VectorF {
        return VectorF(x - operand.x, y - operand.y)
    }

    fun subtract(x: Float, y: Float): VectorF {
        return VectorF(this.x - x, this.y - y)
    }

    fun multiply(operand: VectorF): VectorF {
        return VectorF(x * operand.x, y * operand.y)
    }

    fun multiply(operand: Float): VectorF {
        return VectorF(x * operand, y * operand)
    }

    fun divide(operand: VectorF): VectorF {
        return VectorF(x / operand.x, y / operand.y)
    }

    fun divide(operand: Float): VectorF {
        return VectorF(x / operand, y / operand)
    }

    fun length(): Float {
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }
}