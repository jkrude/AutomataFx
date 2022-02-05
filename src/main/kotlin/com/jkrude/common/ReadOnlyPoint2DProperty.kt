package com.jkrude.common

import javafx.beans.Observable
import javafx.beans.binding.DoubleExpression
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.geometry.Point2D

interface ReadOnlyPoint2DProperty : Observable {

    companion object {
        fun bindToPoint(x1: DoubleProperty, y1: DoubleProperty, other: ReadOnlyPoint2DProperty) {
            x1.bind(other.xProperty)
            y1.bind(other.yProperty)
        }
        fun bindPoints(x1: DoubleProperty, y1: DoubleProperty, x2: ReadOnlyDoubleProperty, y2: ReadOnlyDoubleProperty) {
            x1.bind(x2)
            y1.bind(y2)
        }

        fun bindPoints(x1: DoubleProperty, y1: DoubleProperty, other: ReadOnlyPoint2DProperty) {
            x1.bind(other.xProperty)
            y1.bind(other.yProperty)
        }

        fun unbindPoint(x1: DoubleProperty, y1: DoubleProperty) {
            x1.unbind()
            y1.unbind()
        }

        fun bindPointsBidirectional(x1: DoubleProperty, y1: DoubleProperty, x2: DoubleProperty, y2: DoubleProperty) {
            x1.bindBidirectional(x2)
            y1.bindBidirectional(x2)
        }

        fun unbindPointsBidirectional(x1: DoubleProperty, y1: DoubleProperty, x2: DoubleProperty, y2: DoubleProperty) {
            x1.unbindBidirectional(x2)
            y1.unbindBidirectional(x2)
        }
    }

    val xProperty: ReadOnlyDoubleProperty
    val x: Double get() = xProperty.get()
    val yProperty: ReadOnlyDoubleProperty
    val y: Double get() = yProperty.get()
    val xy: Point2D get() = x x2y y

    fun addOnChange(listener: () -> Unit)

    fun removeOnChange(listener: () -> Unit)

    fun map(transform: (ReadOnlyDoubleProperty) -> DoubleExpression): ObjectBinding<Point2D> =
            objectBindingOf(xProperty, yProperty) {
                transform(xProperty).get() x2y transform(yProperty).get()
            }
}
