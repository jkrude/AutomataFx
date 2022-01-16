package com.jkrude.automata.util

import javafx.beans.InvalidationListener
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.ChangeListener



class Point2DProperty(
    val x: DoubleProperty, val y: DoubleProperty,
    override val changeListener: MutableList<ChangeListener<in Point2DProperty>>,
    override val invalidationListener: MutableList<InvalidationListener>
) : DefaultObservableValue<Point2DProperty> {

    constructor(x: DoubleProperty, y: DoubleProperty) : this(x, y, ArrayList(), ArrayList())
    constructor(x: Double, y: Double) : this(SimpleDoubleProperty(x), SimpleDoubleProperty(y))
    constructor() : this(SimpleDoubleProperty(), SimpleDoubleProperty())

    override fun getValue(): Point2DProperty = this

}