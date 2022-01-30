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
    val start: Point2DProperty = Point2DProperty(startX, startY)
    val end: Point2DProperty = Point2DProperty(endX, endY)
    val control: Point2DProperty = Point2DProperty()
    val isBendedProperty: BooleanProperty = SimpleBooleanProperty(false)
    private var isBended: Boolean by asValue<Boolean>(isBendedProperty)

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
        arrowTip.layoutXProperty().bind(end.xProperty)
        arrowTip.layoutYProperty().bind(end.yProperty)
        super.getChildren().addAll(tail, arrowTip)

        // Apply styling
        arrowTip.fillProperty().bind(tail.strokeProperty())
        val dx: DoubleBinding = end.xProperty.subtract(start.xProperty)
        val dy: DoubleBinding = end.yProperty.subtract(start.yProperty)
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
                val dist = start.xy.distance(end.xy)
                val angle = control.xy.angle(start.xy, end.xy)
                curve.largeArcFlagProperty().set(angle < 90)
                curve.radiusX = dist / (2 * sin(Math.toRadians(angle)))
            }
            control.bind(start)
            control.xProperty.bind(start.xProperty.subtract(start.xProperty.subtract(end.xProperty).divide(2)))
            control.yProperty.bind(start.yProperty.subtract(start.yProperty.subtract(end.yProperty).divide(2)))
            isBendedProperty.addListener { _ ->
                control.unbind()
                tail.elements.remove(line)
                tail.elements.add(curve)
                line.unbind()
                curve.bindXY(end)
                start.addListener { _ -> updateTail() }
                control.addListener { _ -> updateTail() }
                end.addListener { _ -> updateTail() }
            }
            tail.stroke = Values.edgeColor
            tail.fill = null // null such that it cant be clicked
            tail.strokeWidth = 3.0
            moveTo.bindXY(start)
            line.bindXY(end)
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
                        start.yProperty,
                        start.yProperty,
                        end.xProperty,
                        end.yProperty,
                        control.xProperty,
                        control.yProperty
                    )
                }

                override fun computeValue(): Boolean = isToTheRight(start.xy, end.xy, control.xy)

            }
            curve.sweepFlagProperty().bind(toRight)
            tail.setOnMouseDragged {
                isBendedProperty.set(true)
                control.xProperty.set(it.x)
                control.yProperty.set(it.y)
            }
        }
        tailLogic()
    }


    fun adjustArrow(size: Double) {
        arrowAngleProperty.unbind()
        arrowTip.layoutYProperty().unbind()
        arrowTip.layoutXProperty().unbind()
        if (!isBended) {
            val dist = start.xy.distance(end.xy)
            val perR = (dist - size) / dist
            val shortEnd: Point2D = start.xy - (start.xy - end.xy) * perR
            arrowTip.layout = shortEnd
            arrowAngle = Math.toDegrees(atan2(end.y - shortEnd.y, end.x - shortEnd.x))
            return
        }
        val circle = threePointCircle(start.x, start.y, control.x, control.y, end.x, end.y)
        val cx = circle.first.x
        val cy = circle.first.y
        val cr = circle.second
        val reversedScale = if (curve.sweepFlagProperty().get()) -1 else 1
        val endAngle = atan2(end.y - cy, end.x - cx) + reversedScale * size / cr
        val shortEndX = cx + cr * cos(endAngle)
        val shortEndY = cy + cr * sin(endAngle)
        arrowTip.setLayout(shortEndX, shortEndY)
        arrowAngle = Math.toDegrees(atan2(end.y - shortEndY, end.x - shortEndX))
    }

    fun bend(toRight: Boolean = true) {
        isBended = true
        val mid = start.xy.midpoint(end.xy)
        val pivot: Point2D = LineFrom.calcEnd(mid, end.xy, 30.0)
        val angle: Double = if (toRight) -90.0 else 90.0
        control.xy = Rotate(Math.toDegrees(angle), pivot.x, pivot.y).transform(mid.x, mid.y)
    }

}


