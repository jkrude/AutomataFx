package com.jkrude.common.shapes

import com.jkrude.common.*
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableMap
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.ToggleGroup
import javafx.scene.shape.Arc
import javafx.scene.shape.Polygon
import javafx.scene.transform.Rotate
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class SelfLoop(val vertex: VertexView<*>, toggleGroup: ToggleGroup) : DefaultToggle {

    private val arc = Arc()
    private var arrowTip: Polygon = Polygon(
            0.0,
            0.0,
            0.0 - 16,
            0.0 + 8,
            0.0 - 16,
            0.0 - 8
    )
    private val group = Group(arc,arrowTip)
    val a = 1.5
    val radiusDiff = 0.75
    var length = 289.0
    var angle = 216.0

    override val isSelected: BooleanProperty = object : SimpleBooleanProperty() {
        override fun invalidated() {
            arc.stroke = if (this.get()) Values.selectedColor else Values.edgeColor
        }
    }
    override val toggleGroupProperty: ObjectProperty<ToggleGroup> = SimpleObjectProperty(toggleGroup)


    init {
        arc.fill = null
        arc.stroke = Values.edgeColor
        arc.strokeWidth = 3.0
        arc.centerX = vertex.xyProperty.x + vertex.size * a
        arc.centerY = vertex.xyProperty.y
        bindToDistance()
        arc.radiusXProperty().bind(vertex.sizeProperty.multiply(radiusDiff))
        arc.radiusYProperty().bind(arc.radiusXProperty())
        arc.length = length
        vertex.xyProperty.addOnChange { update() }
        arc.setOnMouseDragged {
            update(it.xy)
        }
        arrowTip.fillProperty().bind(arc.fillProperty())
        val rotate = Rotate(0.0,0.0,0.0,1.0,Rotate.Z_AXIS)
        rotate.angleProperty().bind(arc.startAngleProperty())
        arrowTip.transforms.add(rotate)
        update()
    }

    private fun bindToDistance() {
        val d = vertex.xyProperty.xy - arc.center
        arc.centerXProperty().bind(vertex.xyProperty.xProperty.subtract(d.x))
        arc.centerYProperty().bind(vertex.xyProperty.yProperty.subtract(d.y))
    }

    private fun update() {
        val cxy = vertex.xyProperty.xy
        val anchor = (cxy.x + vertex.size * a) x2y cxy.y
        val angleOffset = cxy.angle(anchor, arc.center) * if (anchor.y > arc.centerY) 1 else -1
        arc.startAngle = angleOffset + angle
    }

    private fun update(mouse: Point2D) {
        arc.centerXProperty().unbind()
        arc.centerYProperty().unbind()
        val cxy = vertex.xyProperty.xy
        val d = (mouse).distance(cxy)
        val perR = (vertex.size * a) / d
        val newCenter = cxy - (cxy - mouse) * perR
        arc.centerX = newCenter.x
        arc.centerY = newCenter.y
        update()
        bindToDistance()
    }

    fun getDrawable(): Node = arc

    override fun getUserData(): Any = arc.userData

    override fun setUserData(p0: Any?) {
        arc.userData = p0
    }

    override fun getProperties(): ObservableMap<Any, Any> = arc.properties


}