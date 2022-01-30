package com.jkrude.common

import javafx.geometry.Point2D
import kotlin.math.pow
import kotlin.math.sqrt


data class MathLine(val m: Double, val n: Double)

private fun Double.sq(): Double = this * this


infix fun Point2D.lineTo(to: Point2D): MathLine {
    val m: Double = (this.y - to.y) / (this.x - to.x)
    val n = this.y - m * this.x
    return MathLine(m, n)
}

fun intersection(g: MathLine, h: MathLine): Point2D {
    val x = (h.n - g.n) / (g.m - h.m)
    val y = g.m * x + g.n
    return x x2y y
}

fun isToTheRight(start: Point2D, end: Point2D, toCheck: Point2D): Boolean {
    return ((end.x - start.x) * (toCheck.y - start.y) - (end.y - start.y) * (toCheck.x - start.x)) < 0
}

// Taken from https://www.geeksforgeeks.org/equation-of-circle-when-three-points-on-the-circle-are-given/
fun threePointCircle(
    x1: Double, y1: Double,
    x2: Double, y2: Double,
    x3: Double, y3: Double
): Pair<Point2D, Double> {
    val x12 = x1 - x2
    val x13 = x1 - x3
    val y12 = y1 - y2
    val y13 = y1 - y3
    val y31 = y3 - y1
    val y21 = y2 - y1
    val x31 = x3 - x1
    val x21 = x2 - x1

    // x1^2 - x3^2
    val sx13 = (x1.pow(2.0) - x3.pow(2.0))

    // y1^2 - y3^2
    val sy13 = (y1.pow(2.0) - y3.pow(2.0))
    val sx21 = (x2.pow(2.0) - x1.pow(2.0))
    val sy21 = (y2.pow(2.0) - y1.pow(2.0))
    val f = ((sx13 * x12 + sy13 * x12 + sx21 * x13 + sy21 * x13)
            / (2 * (y31 * x12 - y21 * x13)))
    val g = ((sx13 * y12 + sy13 * y12 + sx21 * y13 + sy21 * y13)
            / (2 * (x31 * y12 - x21 * y13)))
    val c = (-x1.pow(2.0)) - y1.pow(2.0) - 2.0 * g * x1 - 2.0 * f * y1

    // eqn of circle be x^2 + y^2 + 2*g*x + 2*f*y + c = 0
    // where centre is (h = -g, k = -f) and radius r
    // as r^2 = h^2 + k^2 - c
    val h = -g
    val k = -f
    val sqrOfR = h * h + k * k - c

    // r is the radius
    val r = sqrt(sqrOfR)
    return h x2y k to r
}


operator fun Point2D.minus(other: Point2D): Point2D = this.x - other.x x2y this.y - other.y
operator fun Point2D.plus(other: Point2D): Point2D = this.x + other.x x2y this.y + other.y
operator fun Point2D.times(scalar: Double): Point2D = this.x * scalar x2y this.y * scalar

infix fun Double.x2y(y: Double) = Point2D(this, y)
