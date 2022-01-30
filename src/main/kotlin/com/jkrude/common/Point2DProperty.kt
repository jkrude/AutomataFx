package com.jkrude.common

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.binding.DoubleExpression
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.ChangeListener
import javafx.geometry.Point2D

class Point2DProperty(x: Double = 0.0, y: Double = 0.0) : Observable {

    constructor(initial: Point2D) : this(initial.x, initial.y)

    companion object {
        fun boundBy(xProperty: ReadOnlyDoubleProperty, yProperty: ReadOnlyDoubleProperty): Point2DProperty {
            val point = Point2DProperty(xProperty.get(), yProperty.get())
            point.xProperty.bind(xProperty)
            point.yProperty.bind(yProperty)
            return point
        }

        fun bidirectionalBoundBy(xProperty: DoubleProperty, yProperty: DoubleProperty): Point2DProperty {
            val point = Point2DProperty(xProperty.get(), yProperty.get())
            point.xProperty.bindBidirectional(xProperty)
            point.yProperty.bindBidirectional(yProperty)
            return point
        }

        fun bindTo(xProperty: DoubleProperty, yProperty: DoubleProperty): Point2DProperty {
            val point = Point2DProperty(xProperty.get(), yProperty.get())
            xProperty.bind(point.xProperty)
            yProperty.bind(point.yProperty)
            return point
        }

        fun bindPoints(x1: DoubleProperty, y1: DoubleProperty, x2: ReadOnlyDoubleProperty, y2: ReadOnlyDoubleProperty) {
            x1.bind(x2)
            y1.bind(x2)
        }

        fun bindPoints(x1: DoubleProperty, y1: DoubleProperty, other: Point2DProperty) {
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

    val xProperty: DoubleProperty = SimpleDoubleProperty(x)
    var x: Double by asValue(xProperty)
    val yProperty: DoubleProperty = SimpleDoubleProperty(y)
    var y: Double by asValue(yProperty)
    var xy: Point2D
        get() = x x2y y
        set(value) {
            this.x = value.x
            this.y = value.y
        }

    fun set(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    fun set(point: Point2D) {
        this.x = point.x
        this.y = point.y
    }

    fun bind(otherPoint: Point2DProperty) {
        this.xProperty.bind(otherPoint.xProperty)
        this.yProperty.bind(otherPoint.yProperty)
    }

    fun bindBidirectional(otherPoint: Point2DProperty) {
        this.xProperty.bindBidirectional(otherPoint.xProperty)
        this.yProperty.bindBidirectional(otherPoint.yProperty)
    }

    fun unbindBidirectional(otherPoint: Point2DProperty) {
        this.xProperty.unbindBidirectional(otherPoint.xProperty)
        this.yProperty.unbindBidirectional(otherPoint.yProperty)
    }

    fun unbind() {
        this.xProperty.unbind()
        this.yProperty.unbind()
    }

    fun addListener(p0: ChangeListener<in Number>) {
        xProperty.addListener(p0)
        yProperty.addListener(p0)
    }

    override fun addListener(p0: InvalidationListener) {
        xProperty.addListener(p0)
        yProperty.addListener(p0)
    }

    override fun removeListener(p0: InvalidationListener) {
        xProperty.removeListener(p0)
        yProperty.removeListener(p0)
    }

    fun removeListener(p0: ChangeListener<in Number>) {
        xProperty.removeListener(p0)
        yProperty.removeListener(p0)
    }

    fun map(transform: (DoubleProperty) -> DoubleExpression): ObjectBinding<Point2D> =
        objectBindingOf(xProperty, yProperty) {
            transform(xProperty).get() x2y transform(xProperty).get()
        }

}

