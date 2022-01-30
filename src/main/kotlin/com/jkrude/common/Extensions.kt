package com.jkrude.common

import javafx.beans.binding.ObjectBinding
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.shape.*

private fun Double.sq(): Double = this * this
infix fun Point2D.lineTo(to: Point2D): MathLine {
    val m: Double = (this.y - to.y) / (this.x - to.x)
    val n = this.y - m * this.x
    return MathLine(m, n)
}

operator fun Point2D.minus(other: Point2D): Point2D = this.x - other.x x2y this.y - other.y
operator fun Point2D.plus(other: Point2D): Point2D = this.x + other.x x2y this.y + other.y
operator fun Point2D.times(scalar: Double): Point2D = this.x * scalar x2y this.y * scalar
infix fun Double.x2y(y: Double) = Point2D(this, y)

fun LineTo.bindXY(point: Point2DProperty) = Point2DProperty.bindPoints(this.xProperty(), this.yProperty(), point)
fun LineTo.unbind() = Point2DProperty.unbindPoint(this.xProperty(), this.yProperty())
fun ArcTo.bindXY(point: Point2DProperty) = Point2DProperty.bindPoints(this.xProperty(), this.yProperty(), point)
fun ArcTo.unbind() = Point2DProperty.unbindPoint(this.xProperty(), this.yProperty())
fun MoveTo.bindXY(point: Point2DProperty) = Point2DProperty.bindPoints(this.xProperty(), this.yProperty(), point)
fun MoveTo.unbind() = Point2DProperty.unbindPoint(this.xProperty(), this.yProperty())
fun Shape.bindLayout(point: Point2DProperty) =
    Point2DProperty.bindPoints(this.layoutXProperty(), this.layoutYProperty(), point)

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

fun Node.bindLayout(point: Point2DProperty) =
    Point2DProperty.bindPoints(this.layoutXProperty(), this.layoutYProperty(), point)

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

val MouseEvent.xy get() = this.x x2y this.y
fun Circle.bindCenter(point: Point2DProperty) =
    Point2DProperty.bindPoints(this.centerXProperty(), this.centerYProperty(), point)

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

operator fun Point2D.component1(): Double = this.x
operator fun Point2D.component2(): Double = this.y


