package com.jkrude.common.shapes

import com.jkrude.common.*
import com.jkrude.common.logic.Edge
import com.jkrude.common.logic.LabeledNode
import javafx.beans.binding.DoubleBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableMap
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.ToggleGroup
import javafx.scene.paint.Color
import javafx.scene.shape.*
import javafx.scene.transform.Rotate
import kotlin.math.atan2
import kotlin.math.max

class SelfLoop<V : LabeledNode, E : Edge<V>>(
    private val vertex: VertexView<V>,
    override val edgeLogic: E,
    toggleGroup: ToggleGroup
) : EdgeView<V, E> {

    private val moveTo = MoveTo()
    private val arcTo = ArcTo()
    private val path = Path(moveTo, arcTo)
    private var arrowTip: Polygon = Polygon(
        0.0,
        0.0,
        0.0 - 16,
        0.0 + 8,
        0.0 - 16,
        0.0 - 8
    )
    private val relativeDistance = 1.5 // distance of arcCenter to vertex in relation to vertex.size
    private val radiusDiff = 0.6 // arcTo.radius = vertex.size * radiusDiff

    private val arcCenter = Point2DProperty()
    private var lastDist: Point2D
    override val group = Group(path, arrowTip)
    override val midAnchor: ReadOnlyPoint2DProperty
        get() = arcCenter
    override val from: VertexView<V> get() = vertex
    override val to: VertexView<V> get() = vertex

    override val isSelected: BooleanProperty = object : SimpleBooleanProperty() {
        override fun invalidated() {
            path.stroke = if (this.get()) Values.selectedColor else Values.edgeColor
        }
    }
    override val toggleGroupProperty: ObjectProperty<ToggleGroup> =
        SimpleObjectProperty(toggleGroup)


    init {
        //styling
        path.fill = Color.TRANSPARENT // Whole circle is possible event target
        path.stroke = Values.edgeColor
        path.strokeWidth = 3.0
        // initial position
        arcCenter.xy = vertex.xyProperty.x + vertex.size * relativeDistance x2y vertex.xyProperty.y
        lastDist = arcCenter.xy - vertex.xyProperty.xy
        // arcTo settings
        arcTo.radiusXProperty().bind(vertex.sizeProperty.multiply(radiusDiff))
        arcTo.radiusYProperty().bind(arcTo.radiusXProperty())
        arcTo.isLargeArcFlag = true

        // update when dependencies change
        vertex.xyProperty.addOnChange { updateIntersections() }
        group.setOnMouseDragged { updateArcCenter(it.xy) }
        vertex.sizeProperty.addListener { _ -> updateOnSize() }

        // select on click
        group.setOnMousePressed{
            this.toggleGroupProperty().get().selectToggle(this)
        }

        // ArrowTip (Don't ask its complicated)
        arrowTip.fillProperty().bind(path.strokeProperty())
        val rotate = Rotate(0.0, 0.0, 0.0, 1.0, Rotate.Z_AXIS)
        val pointingTo = Point2DProperty()
        val distanceBinding = arcCenter.distance(vertex.xyProperty)
        pointingTo.xProperty.bind(
            arcCenter.xProperty.subtract(
                arcCenter.xProperty.subtract(vertex.xyProperty.xProperty)
                    .multiply(arcTo.radiusXProperty().add(10).divide(distanceBinding))
            )
        )
        pointingTo.yProperty.bind(
            arcCenter.yProperty.subtract(
                arcCenter.yProperty.subtract(vertex.xyProperty.yProperty)
                    .multiply(arcTo.radiusXProperty().add(10).divide(distanceBinding))
            )
        )
        val dx: DoubleBinding = pointingTo.xProperty.subtract(arcTo.xProperty())
        val dy: DoubleBinding = pointingTo.yProperty.subtract(arcTo.yProperty())
        val angleBinding: ObjectBinding<Double> = objectBindingOf(dx, dy) {
            Math.toDegrees(atan2(dy.get(), dx.get()))
        }
        rotate.angleProperty().bind(angleBinding)
        arrowTip.layoutXProperty().bind(arcTo.xProperty())
        arrowTip.layoutYProperty().bind(arcTo.yProperty())
        arrowTip.scaleYProperty().bind(arrowTip.scaleXProperty())
        arrowTip.transforms.add(rotate)

        // Initial update
        updateIntersections()
    }

    private fun updateOnSize() {
        arrowTip.scaleX = max(vertex.size / 100.0, 0.7)
        updateArcCenter(arcCenter.xy)
    }

    private fun updateArcCenter(mouse: Point2D) {
        val cxy = vertex.xyProperty.xy
        val d = (mouse).distance(cxy)
        val perR = (vertex.size * relativeDistance) / d
        arcCenter.xy = cxy - (cxy - mouse) * perR
        lastDist = vertex.xyProperty.xy - arcCenter.xy
        updateIntersections()
    }

    private fun updateIntersections() {
        // update arcCenter
        arcCenter.xy = vertex.xyProperty.xy - lastDist
        val cxy = vertex.xyProperty.xy
        val intersections = circleCircleIntersection(cxy, vertex.size, arcCenter.xy, arcTo.radiusX)
        if (intersections.size != 2) throw java.lang.IllegalStateException()
        val (start, end) = intersections
        moveTo.x = start.x
        moveTo.y = start.y
        arcTo.x = end.x
        arcTo.y = end.y
    }

    override fun getDrawable(): Node = group

    override fun getUserData(): Any = group.userData

    override fun setUserData(p0: Any?) {
        group.userData = p0
    }

    override fun getProperties(): ObservableMap<Any, Any> = group.properties
    override fun isBent() = true

    // TODO
    override fun bend(toRight: Boolean) {}

}