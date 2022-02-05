package com.jkrude.common

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.binding.DoubleExpression
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.SimpleDoubleProperty
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
        set(value) = this.set(value)
    private val onChangeListener: MutableList<() -> Unit> = ArrayList()
    private val invalidationListener: MutableList<InvalidationListener> = ArrayList()
    private var paused = false
    private val xyListener: InvalidationListener = InvalidationListener {
        if (!paused) {
            onChangeListener.forEach { it.invoke() }
            invalidationListener.forEach { it.invalidated(this) }
        }
    }

    init {
        xProperty.addListener(xyListener)
        yProperty.addListener(xyListener)
    }

    fun set(x: Double, y: Double) {
        paused = true
        this.x = x
        this.y = y
        paused = false
        xyListener.invalidated(this)
    }

    fun set(point: Point2D) {
        set(point.x, point.y)
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

    override fun addListener(p0: InvalidationListener) {
        invalidationListener.add(p0)
    }

    override fun removeListener(p0: InvalidationListener) {
        invalidationListener.add(p0)
    }

    fun addOnChange(listener: () -> Unit) {
        onChangeListener.add(listener)
    }

    fun removeOnChange(listener: () -> Unit) {
        onChangeListener.remove(listener)
    }

    fun map(transform: (DoubleProperty) -> DoubleExpression): ObjectBinding<Point2D> =
        objectBindingOf(xProperty, yProperty) {
            transform(xProperty).get() x2y transform(yProperty).get()
        }

}

