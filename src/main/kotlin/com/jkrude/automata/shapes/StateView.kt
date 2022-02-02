package com.jkrude.automata.shapes

import com.jkrude.automata.logic.State
import com.jkrude.common.asValue
import com.jkrude.common.bindCenter
import com.jkrude.common.shapes.CircleView
import com.jkrude.common.x2y
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Point2D
import javafx.scene.control.ToggleGroup
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle

class StateView(initialPoint: Point2D = 0.0 x2y 0.0, toggleGroup: ToggleGroup, vertex: State) :
    CircleView<State>(initialPoint, toggleGroup, vertex) {

    private val finalStateCircle = Circle()
    val isFinalProperty: BooleanProperty = SimpleBooleanProperty(vertex.isFinal)
    var isFinal by asValue(isFinalProperty)
        private set

    init {
        finalStateCircle.visibleProperty().bind(isFinalProperty)
        finalStateCircle.radiusProperty().bind(super.sizeProperty.subtract(5))
        finalStateCircle.bindCenter(super.xyProperty)
        super.group.addEventFilter(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY && it.clickCount == 2) {
                this.isFinal = !this.isFinal
            }
            it.consume()
        }
        finalStateCircle.stroke = Color.WHITE
        finalStateCircle.strokeWidth = 1.8
        finalStateCircle.fill = null
        super.group.children.add(finalStateCircle)
    }

}