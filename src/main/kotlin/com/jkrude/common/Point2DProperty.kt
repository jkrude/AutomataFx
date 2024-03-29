package com.jkrude.common

import javafx.beans.InvalidationListener
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Point2D

class Point2DProperty(x: Double = 0.0, y: Double = 0.0) : ReadOnlyPoint2DProperty {

    constructor(initial: Point2D) : this(initial.x, initial.y)

    override val xProperty: DoubleProperty = SimpleDoubleProperty(x)
    override var x: Double by asValue(xProperty)
    override val yProperty: DoubleProperty = SimpleDoubleProperty(y)
    override var y: Double by asValue(yProperty)
    override var xy: Point2D
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

    override fun addOnChange(listener: () -> Unit) {
        onChangeListener.add(listener)
    }

    override fun removeOnChange(listener: () -> Unit) {
        onChangeListener.remove(listener)
    }

}

