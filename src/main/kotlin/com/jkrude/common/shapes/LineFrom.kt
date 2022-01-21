package com.jkrude.common.shapes

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.shape.Line
import kotlin.math.sqrt

class LineFrom(
    val slope: DoubleProperty,
    val length: DoubleProperty,
    val toGreaterFrom: BooleanProperty
) : Line(){
    constructor(
        fromX: Double,
        fromY: Double,
        slope: Double,
        length: Double,
        toGreaterFrom: Boolean
    ) : this(
        SimpleDoubleProperty(slope),
        SimpleDoubleProperty(length),
        SimpleBooleanProperty(toGreaterFrom)
    ){
        this.startXProperty().value = fromX
        this.startYProperty().value = fromY
    }

    init {
        startXProperty().addListener { _ -> update() }
        startYProperty().addListener { _ -> update() }
        slope.addListener { _ -> update() }
        length.addListener { _ -> update() }
        toGreaterFrom.addListener { _ -> update() }
    }

    private fun update() {
        val onePlusSlope = 1 + slope.get() * slope.get()
        val a1 = (length.get() * sqrt(onePlusSlope)) / onePlusSlope
        endXProperty().value = startX + if (toGreaterFrom.get()) a1 else -a1
        endYProperty().value = startY + if (toGreaterFrom.get()) a1 * slope.get() else -a1 * slope.get()
    }
    companion object {

        fun calcEnd(fromX: Double, fromY: Double, slope: Double, length: Double, toGreaterFrom: Boolean): Pair<Double, Double> {
            val onePlusSlope = 1 + slope * slope
            val a1 = (length * sqrt(onePlusSlope)) / onePlusSlope
            val endX = fromX + if (toGreaterFrom) a1 else -a1
            val endY = fromY + if (toGreaterFrom) a1 * slope else -a1 * slope
            return endX to endY
        }
    }

}