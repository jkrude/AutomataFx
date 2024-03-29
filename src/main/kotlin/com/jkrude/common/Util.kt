package com.jkrude.common

import javafx.beans.binding.DoubleBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Point2D
import kotlin.math.pow
import kotlin.math.sqrt


data class MathLine(val m: Double, val n: Double)


fun intersection(g: MathLine, h: MathLine): Point2D {
    val x = (h.n - g.n) / (g.m - h.m)
    val y = g.m * x + g.n
    return x x2y y
}

fun isToTheRight(start: Point2D, end: Point2D, toCheck: Point2D): Boolean {
    return ((end.x - start.x) * (toCheck.y - start.y) - (end.y - start.y) * (toCheck.x - start.x)) < 0
}

// Returns the vector representing the vector projection of this on b
// https://en.wikipedia.org/wiki/Vector_projection
infix fun Point2D.projectOn(b: Point2D): Point2D {
    val n = b.normalize()
    return (n * this.dotProduct(n))
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
fun threePointCircle(start: Point2D, control: Point2D, end: Point2D) =
        threePointCircle(start.x, start.y, control.x, control.y, end.x, end.y)

fun <T> objectBindingOf(vararg dependencies: ObservableValue<*>, compute: () -> T): ObjectBinding<T> {
    return object : ObjectBinding<T>() {
        init {
            bind(*dependencies)
        }

        override fun getDependencies(): ObservableList<*> = FXCollections.observableArrayList(*dependencies)
        override fun computeValue(): T = compute()
        override fun dispose() {
            unbind(*dependencies)
        }
    }
}
fun doubleBindingOf(vararg dependencies: ObservableValue<*>, compute: () -> Double): DoubleBinding {
    return object : DoubleBinding() {
        init {
            bind(*dependencies)
        }

        override fun computeValue(): Double {
            return compute()
        }

        override fun getDependencies(): ObservableList<*> = FXCollections.observableArrayList(*dependencies)

        override fun dispose() {
            unbind(*dependencies)
        }
    }
}

//stackoverflow.com/questions/3349125/circle-circle-intersection-points
fun circleCircleIntersection(c1: Point2D, r1: Double, c2: Point2D, r2: Double): List<Point2D> {
    val d = c1.distance(c2)
    if (d > r1 + r2) return emptyList()
    val a: Double = (r1.sq() - r2.sq() + d.sq()) / (2 * d)
    val p2 = c1 - ((c1 - c2) * a) / d
    if (d == r1 + r2) return listOf(p2)
    val h = sqrt(r1.sq() - a.sq())
    val x3 = p2.x + h * (c2.y - c1.y) / d
    val y3 = p2.y - h * (c2.x - c1.x) / d
    val x4 = p2.x - h * (c2.y - c1.y) / d
    val y4 = p2.y + h * (c2.x - c1.x) / d
    return listOf(x4 x2y y4, x3 x2y y3)
}
