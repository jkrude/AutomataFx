package com.jkrude.automata.shapes

import com.jkrude.common.DefaultToggle
import com.jkrude.common.Point2DProperty
import com.jkrude.common.Values
import com.jkrude.common.minus
import com.jkrude.common.shapes.Arrow
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableMap
import javafx.scene.control.ToggleGroup

class StartEdge(val start: Point2DProperty, val initialState: StateView, toggleGroup: ToggleGroup) : DefaultToggle {

    val arrow = Arrow(start,initialState.xyProperty)
    override val toggleGroupProperty: ObjectProperty<ToggleGroup> = SimpleObjectProperty(toggleGroup)

    init {
        arrow.isBendable = false
        arrow.setOnMouseDragged {
            start.unbind()
            start.x = it.x
            start.y = it.y
            bindToDistance()
        }
        bindToDistance()
        start.addOnChange { arrow.adjustArrow(initialState.size) }
        initialState.xyProperty.addOnChange { arrow.adjustArrow(initialState.size) }
        initialState.sizeProperty.addListener { _ -> arrow.adjustArrow(initialState.size) }
        arrow.setOnMousePressed {
            this.toggleGroupProperty.get().selectToggle(this)
        }
        arrow.adjustArrow(initialState.size)
    }
    private fun bindToDistance(){
        val d =  initialState.xyProperty.xy -  start.xy
        start.xProperty.bind(initialState.xyProperty.xProperty.subtract(d.x))
        start.yProperty.bind(initialState.xyProperty.yProperty.subtract(d.y))
    }

    override val isSelected: BooleanProperty = object : SimpleBooleanProperty() {
        override fun invalidated() {
            arrow.color = if (isSelected()) Values.selectedColor else Values.primaryColor
        }
    }

    override fun getUserData(): Any = arrow.userData

    override fun setUserData(p0: Any?) {
        arrow.userData = p0
    }

    override fun getProperties(): ObservableMap<Any, Any> = arrow.properties

}