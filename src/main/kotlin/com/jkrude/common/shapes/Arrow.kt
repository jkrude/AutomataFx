package com.jkrude.common.shapes

import com.jkrude.common.*
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.DoubleBinding
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.control.ToggleGroup
import javafx.scene.shape.*
import javafx.scene.transform.Rotate
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

open class Arrow(startX: Double = 0.0, startY: Double = 0.0, endX: Double = 0.0, endY: Double = 50.0) : Group(),
    DefaultToggle {

    private val moveTo = MoveTo()
    private val curve = ArcTo()
    private val line = LineTo()
    private val tail = Path(moveTo, line) // start with straight line
    private var arrowTip: Polygon = Polygon(
        0.0,
        0.0,
        0.0 - 16,
        0.0 + 8,
        0.0 - 16,
        0.0 - 8
    )
    private val arrowAngleProperty: DoubleProperty
    private var arrowAngle: Double
        get() = arrowAngleProperty.get()
        set(value) {
            arrowAngleProperty.set(value)
        }
    val startXProperty: DoubleProperty = SimpleDoubleProperty(startX)
    var startX by DelegatedDoubleProperty(startXProperty)
    val startYProperty: DoubleProperty = SimpleDoubleProperty(startY)
    var startY by DelegatedDoubleProperty(startYProperty)
    val endXProperty: DoubleProperty = SimpleDoubleProperty(endX)
    var endX by DelegatedDoubleProperty(endXProperty)
    val endYProperty: DoubleProperty = SimpleDoubleProperty(endY)
    var endY by DelegatedDoubleProperty(endYProperty)
    val start get() = startX x2y startY
    val end get() = endX x2y endY
    val controlXProperty: DoubleProperty = SimpleDoubleProperty()
    val controlX: Double by DelegatedDoubleProperty(controlXProperty)
    val controlYProperty: DoubleProperty = SimpleDoubleProperty()
    var controlY: Double by DelegatedDoubleProperty(controlYProperty)
    var control
        get() = controlX x2y controlY
        set(value) {
            this.controlXProperty.value = value.x
            this.controlYProperty.value = value.y
        }
    val isBendedProperty: BooleanProperty = SimpleBooleanProperty(false)
    private var isBended: Boolean by DelegatedBooleanProperty(isBendedProperty)

    override val toggleGroupProperty: ObjectProperty<ToggleGroup> = SimpleObjectProperty()
    override val isSelected: BooleanProperty = object : SimpleBooleanProperty() {
        override fun invalidated() {
            this@Arrow.tail.stroke = if (this.get()) Values.selectedColor else Values.edgeColor
        }
    }


    init {
        // Arrow tip logic
        val rotate = Rotate(0.0, 0.0, 0.0, 1.0, Rotate.Z_AXIS)
        arrowAngleProperty = rotate.angleProperty()
        arrowTip.transforms.add(rotate)
        arrowTip.layoutXProperty().bind(endXProperty)
        arrowTip.layoutYProperty().bind(endYProperty)
        super.getChildren().addAll(tail, arrowTip)

        // Apply styling
        arrowTip.fillProperty().bind(tail.strokeProperty())
        val dx: DoubleBinding = endXProperty.subtract(startXProperty)
        val dy: DoubleBinding = endYProperty.subtract(startYProperty)
        val angleBinding: DoubleBinding = object : DoubleBinding() {
            init {
                dx.addListener { _ -> this.invalidate() }
                dy.addListener { _ -> this.invalidate() }
            }

            override fun computeValue(): Double = Math.toDegrees(atan2(dy.get(), dx.get()))
        }
        arrowAngleProperty.bind(angleBinding)

        fun tailLogic() {

            fun updateTail() {
                val dist = start.distance(end)
                val angle = control.angle(start, end)
                curve.largeArcFlagProperty().set(angle < 90)
                curve.radiusX = dist / (2 * sin(Math.toRadians(angle)))
            }
            controlXProperty.bind(startXProperty.subtract(startXProperty.subtract(endXProperty).divide(2)))
            controlYProperty.bind(startYProperty.subtract(startYProperty.subtract(endYProperty).divide(2)))
            isBendedProperty.addListener { _ ->
                controlXProperty.unbind()
                controlYProperty.unbind()
                tail.elements.remove(line)
                tail.elements.add(curve)
                line.xProperty().unbind()
                line.yProperty().unbind()
                curve.xProperty().bind(endXProperty)
                curve.yProperty().bind(endYProperty)
                startXProperty.addListener { _ -> updateTail() }
                startYProperty.addListener { _ -> updateTail() }
                controlXProperty.addListener { _ -> updateTail() }
                controlYProperty.addListener { _ -> updateTail() }
                endXProperty.addListener { _ -> updateTail() }
                endYProperty.addListener { _ -> updateTail() }
            }
            tail.stroke = Values.edgeColor
            tail.fill = null // null such that it cant be clicked
            tail.strokeWidth = 3.0
            moveTo.xProperty().bind(startXProperty)
            moveTo.yProperty().bind(startYProperty)
            line.xProperty().bind(endXProperty)
            line.yProperty().bind(endYProperty)
            curve.radiusYProperty().bind(curve.radiusXProperty())
            val toRight: BooleanBinding = object : BooleanBinding() {
                init {
                    dependencies.forEach {
                        it.addListener { _ ->
                            this.invalidate()
                        }
                    }
                }

                override fun getDependencies(): ObservableList<Property<*>> {
                    return FXCollections.observableArrayList(
                        startYProperty,
                        startYProperty,
                        endXProperty,
                        endYProperty,
                        controlXProperty,
                        controlYProperty
                    )
                }

                override fun computeValue(): Boolean = isToTheRight(start, end, control)

            }
            curve.sweepFlagProperty().bind(toRight)
            tail.setOnMouseDragged {
                isBendedProperty.set(true)
                controlXProperty.set(it.x)
                controlYProperty.set(it.y)
            }
        }

        tailLogic()
    }


    fun adjustArrow(size: Double) {
        arrowAngleProperty.unbind()
        arrowTip.layoutYProperty().unbind()
        arrowTip.layoutXProperty().unbind()
        if (!isBended) {
            val dist = start.distance(end)
            val perR = (dist - size) / dist
            val shortEnd: Point2D = start - (start - end) * perR
            arrowTip.layoutX = shortEnd.x
            arrowTip.layoutY = shortEnd.y
            arrowAngle = Math.toDegrees(atan2(end.y - shortEnd.y, end.x - shortEnd.x))
            return
        }
        val circle = threePointCircle(startX, startY, controlX, controlY, endX, endY)
        val cx = circle.first.x
        val cy = circle.first.y
        val cr = circle.second
        val reversedScale = if (curve.sweepFlagProperty().get()) -1 else 1
        val endAngle = atan2(endY - cy, endX - cx) + reversedScale * size / cr
        val shortEndX = cx + cr * cos(endAngle)
        val shortEndY = cy + cr * sin(endAngle)
        arrowTip.layoutX = shortEndX
        arrowTip.layoutY = shortEndY
        arrowAngle = Math.toDegrees(atan2(endY - shortEndY, endX - shortEndX))
    }

    fun bend(toRight: Boolean = true) {
        isBended = true
        val mid = start.midpoint(end)
        val pivot: Point2D = LineFrom.calcEnd(mid, end, 30.0)
        val angle: Double = if (toRight) -90.0 else 90.0
        control = Rotate(Math.toDegrees(angle), pivot.x, pivot.y).transform(mid.x, mid.y)
    }

}


