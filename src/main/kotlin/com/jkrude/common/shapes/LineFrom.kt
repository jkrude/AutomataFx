package com.jkrude.common.shapes

import com.jkrude.common.x2y
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Point2D
import javafx.scene.shape.Line
import kotlin.math.sqrt

class LineFrom(
    val slope: DoubleProperty,
    val length: DoubleProperty,
    val toGreaterFrom: BooleanProperty
) : Line() {
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
    ) {
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

        fun calcEnd(fromX: Double, fromY: Double, slope: Double, length: Double, toGreaterFrom: Boolean): Point2D {
            val onePlusSlope = 1 + slope * slope
            val a1 = (length * sqrt(onePlusSlope)) / onePlusSlope
            val endX = fromX + if (toGreaterFrom) a1 else -a1
            val endY = fromY + if (toGreaterFrom) a1 * slope else -a1 * slope
            return endX x2y endY
        }

        fun calcEnd(start: Point2D, end: Point2D, length: Double): Point2D {
            return if (start.x == end.x) start.x x2y start.y + length * if (end.y < start.y) -1 else 1
            else calcEnd(start.x, start.y, (start.y - end.y) / (start.x - end.x), length, start.x < end.x)
        }
    }

}