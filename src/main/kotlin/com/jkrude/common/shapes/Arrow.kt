package com.jkrude.common.shapes

import com.jkrude.common.*
import javafx.beans.binding.DoubleBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Paint
import javafx.scene.shape.*
import javafx.scene.transform.Rotate
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Draggable Arrow that can be moved through 2D.
 * The arrow can be manipulated by the start and end position or by dragging the tail.
 * There are two states depending on whether the tail was dragged or not.
 * At the moment only circular endpoints are supported.
 * The curvature of the bended arrow is defined as a circle over three points.
 */
open class Arrow(startX: Double = 0.0, startY: Double = 0.0, endX: Double = 0.0, endY: Double = 50.0) : Group() {

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
    private val _control: Point2DProperty = Point2DProperty()
    val control: ReadOnlyPoint2DProperty = _control
    val end: Point2DProperty = Point2DProperty(endX, endY)

    // Save control as relative to start and end:
    //relative distance from start and projection of control onto (end - start)
    private var relativeDist: Double = 0.5

    // Distance from projection of control to control
    // negative if control to the right of line between start and end
    private var perpendicularPart: Double = 0.0

    val isBentProperty: BooleanProperty = SimpleBooleanProperty(false)
    private var isBent: Boolean by asValue<Boolean>(isBentProperty)
    val isBendableProperty: BooleanProperty = SimpleBooleanProperty(true)
    var isBendable: Boolean by asValue(isBendableProperty)
    val isSelected: BooleanProperty = object : SimpleBooleanProperty() {
        override fun invalidated() {
            this@Arrow.tail.stroke = if (this.get()) Values.selectedColor else Values.edgeColor
        }
    }
    val colorProperty: ObjectProperty<Paint> = this.tail.strokeProperty()
    var color: Paint by asValue(colorProperty)

    constructor(from: Point2DProperty, to: Point2DProperty) : this(from.x, from.y, to.x, to.y) {
        start.bind(from)
        end.bind(to)
    }

    init {
        // tip logic
        val rotate = Rotate(0.0, 0.0, 0.0, 1.0, Rotate.Z_AXIS)
        arrowAngleProperty = rotate.angleProperty()
        arrowTip.transforms.add(rotate)
        arrowTip.bindLayout(end)
        super.getChildren().addAll(tail, arrowTip)

        arrowTip.fillProperty().bind(tail.strokeProperty())
        val dx: DoubleBinding = end.xProperty.subtract(start.xProperty)
        val dy: DoubleBinding = end.yProperty.subtract(start.yProperty)
        val angleBinding: ObjectBinding<Double> = objectBindingOf(dx, dy) {
            Math.toDegrees(atan2(dy.get(), dx.get()))
        }
        arrowAngleProperty.bind(angleBinding)


        // tail logic
        // If not bend -> draw a straight line and set control on the middle of this line.
        // Otherwise draw arc by three points and update control by methods.
        // Currently if bend once there is no going back to not-bend.
        _control.xProperty.bind(start.xProperty.subtract(start.xProperty.subtract(end.xProperty).divide(2)))
        _control.yProperty.bind(start.yProperty.subtract(start.yProperty.subtract(end.yProperty).divide(2)))
        isBentProperty.addListener { _ ->
            _control.unbind()
            tail.elements.remove(line)
            tail.elements.add(curve)
            line.unbind()
            curve.bindXY(end)
            start.addOnChange(::updateTailFromStartEnd)
            end.addOnChange(::updateTailFromStartEnd)
        }
        tail.stroke = Values.edgeColor
        tail.fill = null // null such that it can't be clicked
        tail.strokeWidth = 3.0  // styling
        moveTo.bindXY(start)
        line.bindXY(end)
        curve.radiusYProperty().bind(curve.radiusXProperty())
        val toRight: ObjectBinding<Boolean> = objectBindingOf(
            start.yProperty,
            start.yProperty,
            end.xProperty,
            end.yProperty,
            _control.xProperty,
            _control.yProperty
        ) {
            isToTheRight(start.xy, end.xy, _control.xy)
        }

        curve.sweepFlagProperty().bind(toRight)
        tail.setOnMouseDragged(::updateTailOnDragged)
    }

    private fun updateTailOnDragged(it: MouseEvent) {
        if (!isBendable) return
        isBentProperty.set(true)
        _control.set(it.x, it.y)
        updateTailFromControl()
    }

    private fun updateTail() {
        val dist = start.xy.distance(end.xy)
        val angle = _control.xy.angle(start.xy, end.xy)
        curve.largeArcFlagProperty().set(angle < 90)
        curve.radiusX = dist / (2 * sin(Math.toRadians(angle)))
    }

    private fun updateTailFromControl() {
        // Called when control was changed.
        calculateControlRelative()
        updateTail()
    }

    private fun updateTailFromStartEnd() {
        // Recalculate control from the saved relative position.
        // Called when eiter start or end was changed.
        val intersection = start.xy - (start.xy - end.xy) * relativeDist
        val vec = (end.xy - start.xy)
        val orth: Point2D = ((vec.y * -1.0) x2y vec.x).normalize()
        _control.xy = intersection + (orth * perpendicularPart)
        updateTail()
    }

    private fun calculateControlRelative() {
        // Save the position of control as relative to start and endpoint as
        // relative distance to start and distance to the line between start and end.
        val cVec = control.xy - start.xy
        val projection = cVec projectOn (end.xy - start.xy)
        val intersection = start.xy + projection
        this.relativeDist = start.xy.distance(intersection) / max(start.xy.distance(end.xy), 1.0)
        this.relativeDist *= if (control.x < start.x && start.x < end.x || control.x > start.x && start.x > end.x) -1.0 else 1.0
        this.perpendicularPart = control.xy.distance(intersection) * if (curve.isSweepFlag) -1.0 else 1.0
    }


    fun adjustArrow(size: Double) {
        // Only the tip is adjusted on size changes from the end-node
        // as the arc will go from-mid point to mid-point.
        arrowAngleProperty.unbind()
        arrowTip.layoutYProperty().unbind()
        arrowTip.layoutXProperty().unbind()
        if (!isBent) {
            val dist = start.xy.distance(end.xy)
            val perR = (dist - size) / dist
            val shortEnd: Point2D = start.xy - (start.xy - end.xy) * perR
            arrowTip.layout = shortEnd
            arrowAngle = Math.toDegrees(atan2(end.y - shortEnd.y, end.x - shortEnd.x))
            return
        }
        val circle = threePointCircle(start.x, start.y, _control.x, _control.y, end.x, end.y)
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
        // Bend node by moving the control point by a length of 30 to the right or left.
        if (!this.isBendable) // TODO bad interface design
            throw IllegalStateException("Bend was called on an un-bendable arrow.")
        isBent = true
        val mid = start.xy.midpoint(end.xy)
        val pivot: Point2D = LineFrom.calcEnd(mid, end.xy, 30.0)
        val angle: Double = if (toRight) -90.0 else 90.0
        _control.xy = Rotate(Math.toDegrees(angle), pivot.x, pivot.y).transform(mid.x, mid.y)
        updateTailFromControl()
    }

}
