package com.jkrude.common

import kotlin.math.sqrt

typealias Point2D = Pair<Double, Double>

data class MathLine(val m: Double, val n: Double)

private fun Double.sq(): Double = this * this

infix fun Point2D.distTo(to: Point2D) =
    sqrt((this.first - to.first).sq() + (this.second - to.second).sq())


infix fun Point2D.lineTo(to: Point2D): MathLine {
    val m: Double = (this.second - to.second) / (this.first - to.first)
    val n = this.second - m * this.first
    return MathLine(m, n)
}

fun intersection(g: MathLine, h: MathLine): Point2D {
    val x = (h.n - g.n) / (g.m - h.m)
    val y = g.m * x + g.n
    return Point2D(x, y)

}