package com.jkrude.common.shapes

import com.jkrude.common.DefaultToggle
import com.jkrude.common.Values
import javafx.beans.property.*
import javafx.scene.Group
import javafx.scene.control.ToggleGroup
import javafx.scene.shape.Line
import javafx.scene.shape.Polygon
import javafx.scene.transform.Rotate
import kotlin.math.atan2

open class Arrow(startX: Double = 0.0, startY: Double = 0.0, endX: Double = 0.0, endY: Double = 50.0) : Group(),
    DefaultToggle {

    val startXProperty: DoubleProperty get() = tail.startXProperty()
    var startX
        get() = startXProperty.get()
        set(value) {
            startXProperty.value = value
        }
    val startYProperty: DoubleProperty get() = tail.startYProperty()
    var startY
        get() = startYProperty.get()
        set(value) {
            startYProperty.value = value
        }
    val endXProperty: DoubleProperty get() = tail.endXProperty()
    var endX
        get() = endXProperty.get()
        set(value) {
            endXProperty.value = value
        }
    val endYProperty: DoubleProperty get() = tail.endYProperty()
    var endY
        get() = endYProperty.get()
        set(value) {
            endXProperty.value = value
        }

    override val toggleGroupProperty: ObjectProperty<ToggleGroup> = SimpleObjectProperty()
    override val isSelected: BooleanProperty = object : SimpleBooleanProperty() {
        override fun invalidated() {
            this@Arrow.tail.stroke = if (this.get()) Values.selectedColor else Values.edgeColor
        }
    }


    private val tail = Line()
//    private val tailHandler: TailHandler

    init {
        val dx = endXProperty.subtract(startXProperty)
        val dy = endYProperty.subtract(startYProperty)
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
        tail.stroke = Values.edgeColor
        tail.fill = null // null such that it cant be clicked
        tail.strokeWidth = 2.0
    }

    private fun getAngle(dy: Double, dx: Double): Double {
        return Math.toDegrees(atan2(dy, dx))
    }

//    inner class TailHandler {
//
//        private val wasDragged: BooleanProperty = SimpleBooleanProperty(false)
//
//        private val updateXonChange = { _: Observable -> updateX() }
//        private val updateYonChange = { _: Observable -> updateY() }
//        private val controlCircle = Circle()
//        init {
//            controlCircle.centerXProperty().bindBidirectional(tail.controlXProperty())
//            controlCircle.centerYProperty().bindBidirectional(tail.controlYProperty())
//            // Styling
//            controlCircle.radius = 4.0
//            controlCircle.fillProperty().bind(tail.strokeProperty())
//            controlCircle.disableProperty().bind(wasDragged.not().or(isSelected.not()))
//
//            controlCircle.setOnMouseDragged { event ->
//                controlCircle.centerX = event.x
//                controlCircle.centerY = event.y
//                wasDragged.value = true
//            }
//            this@Arrow.children.add(controlCircle)
//            controlCircle.toFront()
//            tail.setOnMousePressed {
//                onMousePressed?.handle(it)
//                if (it.isPrimaryButtonDown) toggleGroupProperty.value.selectToggle(this@Arrow)
//            }
//            updateX()
//            updateY()
//            controlCircle.disableProperty().addListener() { _, _ ,new ->
//                if(new) bind()
//                else unbind()
//            }
//        }
//
//        private fun bind(){
//            startXProperty.addListener(updateXonChange)
//            startYProperty.addListener(updateYonChange)
//            endXProperty.addListener(updateXonChange)
//            endYProperty.addListener(updateYonChange)
//        }
//        private fun unbind(){
//            startXProperty.removeListener(updateXonChange)
//            startYProperty.removeListener(updateYonChange)
//            endXProperty.removeListener(updateXonChange)
//            endYProperty.removeListener(updateYonChange)
//        }
//
//        private fun updateX() {
//            tail.controlX = startXProperty.get() - (startXProperty.get() - endXProperty.get()) * 0.5
//        }
//
//        private fun updateY() {
//            tail.controlY = startXProperty.get() - (startYProperty.get() - endYProperty.get()) * 0.5
//        }
//
//    }


}


