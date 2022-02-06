package com.jkrude.common

import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.shape.*

fun Double.sq(): Double = this * this
infix fun Point2D.lineTo(to: Point2D): MathLine {
    val m: Double = (this.y - to.y) / (this.x - to.x)
    val n = this.y - m * this.x
    return MathLine(m, n)
}

operator fun Point2D.minus(other: Point2D): Point2D = this.x - other.x x2y this.y - other.y
operator fun Point2D.plus(other: Point2D): Point2D = this.x + other.x x2y this.y + other.y
operator fun Point2D.times(scalar: Double): Point2D = this.x * scalar x2y this.y * scalar
operator fun Point2D.div(scalar: Double): Point2D = this.x / scalar x2y this.y / scalar
infix fun Double.x2y(y: Double) = Point2D(this, y)

fun LineTo.bindXY(point: ReadOnlyPoint2DProperty) = ReadOnlyPoint2DProperty.bindToPoint(this.xProperty(), this.yProperty(), point)
fun LineTo.unbind() = ReadOnlyPoint2DProperty.unbindPoint(this.xProperty(), this.yProperty())
fun ArcTo.bindXY(point: ReadOnlyPoint2DProperty) = ReadOnlyPoint2DProperty.bindToPoint(this.xProperty(), this.yProperty(), point)
fun ArcTo.unbind() = ReadOnlyPoint2DProperty.unbindPoint(this.xProperty(), this.yProperty())
fun MoveTo.bindXY(point: ReadOnlyPoint2DProperty) = ReadOnlyPoint2DProperty.bindToPoint(this.xProperty(), this.yProperty(), point)
fun MoveTo.unbind() = ReadOnlyPoint2DProperty.unbindPoint(this.xProperty(), this.yProperty())
fun Shape.bindLayout(point: ReadOnlyPoint2DProperty) =
    ReadOnlyPoint2DProperty.bindToPoint(this.layoutXProperty(), this.layoutYProperty(), point)

var Shape.layout: Point2D
    get() = this.layoutX x2y this.layoutY
    set(value) {
        this.layoutX = value.x
        this.layoutY = value.y
    }

fun Shape.setLayout(x: Double, y: Double) {
    this.layoutX = x
    this.layoutY = y
}

fun Node.bindLayout(point: ReadOnlyPoint2DProperty) =
    ReadOnlyPoint2DProperty.bindToPoint(this.layoutXProperty(), this.layoutYProperty(), point)

var Node.layout: Point2D
    get() = this.layoutX x2y this.layoutY
    set(value) {
        this.layoutX = value.x
        this.layoutY = value.y
    }

fun Node.setLayout(x: Double, y: Double) {
    this.layoutX = x
    this.layoutY = y
}
fun Arc.bindCenter(point: ReadOnlyPoint2DProperty) =
        ReadOnlyPoint2DProperty.bindToPoint(this.centerXProperty(), this.centerYProperty(), point)
val Arc.center get() = this.centerX x2y this.centerY

val MouseEvent.xy get() = this.x x2y this.y
fun Circle.bindCenter(point: ReadOnlyPoint2DProperty) =
    ReadOnlyPoint2DProperty.bindToPoint(this.centerXProperty(), this.centerYProperty(), point)

operator fun Point2D.component1(): Double = this.x
operator fun Point2D.component2(): Double = this.y
