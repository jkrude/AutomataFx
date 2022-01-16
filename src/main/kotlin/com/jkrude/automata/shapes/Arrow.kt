package com.jkrude.automata.shapes

import javafx.beans.Observable
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.Group
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Polygon
import javafx.scene.shape.QuadCurve
import javafx.scene.transform.Rotate
import kotlin.math.*

open class Arrow(startX: Double = 0.0, startY: Double = 0.0, endX: Double = 0.0, endY: Double = 50.0) : Group() {

    val startXProperty: DoubleProperty get() = tail.startXProperty()
    val startYProperty: DoubleProperty get() = tail.startYProperty()
    val endXProperty: DoubleProperty get() = tail.endXProperty()
    val endYProperty: DoubleProperty get() = tail.endYProperty()


    val tail: QuadCurve = QuadCurve()
    private val tailHandler: TailHandler

    init {

        val dx = endXProperty.subtract(tail.controlXProperty())
        val dy = endYProperty.subtract(tail.controlYProperty())
        // Arrow head
        val triangle = Polygon(
            endXProperty.value,
            endYProperty.value,
            endXProperty.value - 16,
            endYProperty.value + 8,
            endXProperty.value - 16,
            endYProperty.value - 8
        )
        val rotate = Rotate(0.0, 0.0, 0.0, 1.0, Rotate.Z_AXIS)
        // rotate logic for head
        triangle.transforms.add(rotate)
        dx.addListener { _, _, newValue ->
            rotate.angle = getAngle(
                dy.doubleValue(),
                newValue.toDouble()
            )
        }
        dy.addListener { _, _, newValue ->
            rotate.angle = getAngle(
                newValue.toDouble(),
                dx.doubleValue()
            )
        }
        // initialize
        tail.startX = startX
        tail.startY = startY
        tail.endX = endX
        tail.endY = endY

        triangle.layoutX = tail.endX
        triangle.layoutY = tail.endY
        triangle.layoutXProperty().bind(endXProperty)
        triangle.layoutYProperty().bind(endYProperty)
        super.getChildren().addAll(tail, triangle)

        // Apply styling
        triangle.fillProperty().bind(tail.strokeProperty())
        tail.stroke = Color.DARKGRAY
        tail.fill = null // null sucht that it cant be clicked
        tail.strokeWidth = 2.0

    }

    private fun getAngle(dy: Double, dx: Double): Double {
        return Math.toDegrees(atan2(dy, dx))
    }

    inner class TailHandler {

        private val wasDragged: BooleanProperty = SimpleBooleanProperty(false)

        private val updateXonChange = { _: Observable -> updateX() }
        private val updateYonChange = { _: Observable -> updateY() }
        private val controlCircle = Circle()
        private val controlDistStart = SimpleDoubleProperty()
        private val controlDistEnd = SimpleDoubleProperty()
        private val controlSlopeStart = SimpleDoubleProperty()
        private val controlSlopeEnd = SimpleDoubleProperty()
        private val startSmallerControl = tail.startXProperty().subtract(tail.controlXProperty()).lessThan(0)
        private val controlSmallerEnd = tail.controlXProperty().subtract(tail.endXProperty()).lessThan(0)

        init {
            controlCircle.centerXProperty().bindBidirectional(tail.controlXProperty())
            controlCircle.centerYProperty().bindBidirectional(tail.controlYProperty())
            // Styling
            controlCircle.radius = 4.0
            controlCircle.fillProperty().bind(tail.strokeProperty())
            //controlCircle.disableProperty().bind(wasDragged)

            controlCircle.setOnMouseDragged { event ->
                controlCircle.centerX = event.x
                controlCircle.centerY = event.y
                wasDragged.value = true
            }
            this@Arrow.children.add(controlCircle)
            controlCircle.toFront()
            updateX()
            updateY()
        }

        init {
            tail.controlXProperty().addListener { _ -> calculateControlProperties() }
            tail.controlYProperty().addListener { _ -> calculateControlProperties() }

            startXProperty.addListener { _ -> adjustControlFrom() }
            startYProperty.addListener { _ -> adjustControlFrom() }
            endXProperty.addListener { _ -> adjustControlTo() }
            endYProperty.addListener { _ -> adjustControlTo() }

        }

        private fun updateX() {
            tail.controlX = tail.startX - (tail.startX - tail.endX) * 0.5
        }

        private fun updateY() {
            tail.controlY = tail.startY - (tail.startY - tail.endY) * 0.5
        }

        private fun calculateControlProperties() {
            val fromX = startXProperty.get()
            val fromY = startYProperty.get()
            val toX = endXProperty.get()
            val toY = endYProperty.get()
            val contrX = tail.controlX
            val contrY = tail.controlY
            controlDistStart.value = sqrt((fromX - contrX).sq() + (fromY - contrY).sq())
            controlDistEnd.value = sqrt((contrX - toX).sq() + (contrY - toY).sq())
            controlSlopeStart.value = (fromY - contrY) / (fromX - contrX)
            controlSlopeEnd.value = (contrY - toY) / (contrX - toX)
        }

        private fun adjustControlFrom() {
            val fromX = startXProperty.get()
            val fromY = startYProperty.get()
            val (endX, endY) = LineFrom.calcEnd(
                fromX,
                fromY,
                controlSlopeStart.get(),
                controlDistStart.get(),
                startSmallerControl.get()
            )
            tail.controlX = endX
            tail.controlY = endY
        }

        private fun adjustControlTo() {
            val (endX, endY) = LineFrom.calcEnd(
                tail.endX,
                tail.endY,
                controlSlopeEnd.get(),
                controlDistEnd.get(),
                controlSmallerEnd.get()
            )
            tail.controlX = endX
            tail.controlY = endY
        }
    }

    init {
        tailHandler = TailHandler()
    }


}

private fun Double.sq(): Double = this * this
