package com.jkrude.common.shapes

import com.jkrude.common.DefaultToggle
import com.jkrude.common.Values
import com.jkrude.common.logic.Edge
import com.jkrude.common.logic.LabeledNode
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableMap
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.ToggleGroup

interface EdgeView<V : LabeledNode, E : Edge<V>> {
    val from: VertexView<V>
    val to: VertexView<V>
    val edgeLogic: E

    fun getDrawable(): Node

}

open class DefaultEdgeView<V : LabeledNode, E : Edge<V>>(
    final override val from: VertexView<V>,
    final override val to: VertexView<V>,
    final override val edgeLogic: E,
    toggleGroup: ToggleGroup
) : EdgeView<V, E>, DefaultToggle {

    protected val arrow: Arrow = Arrow(from.xyProperty, to.xyProperty)
    protected val group: Group = Group(arrow)
    override val isSelected: BooleanProperty = object : SimpleBooleanProperty() {
        override fun invalidated() {
            arrow.color = if (this.get()) Values.selectedColor else Values.edgeColor
        }
    }
    override val toggleGroupProperty: ObjectProperty<ToggleGroup> = SimpleObjectProperty(toggleGroup)

    init {
        arrow.start.bind(from.xyProperty)
        arrow.end.bind(to.xyProperty)

        to.xyProperty.addOnChange { arrow.adjustArrow(to.size) }
        to.sizeProperty.addListener { _ -> arrow.adjustArrow(to.size) }
        from.xyProperty.addOnChange { arrow.adjustArrow(to.size) }
        arrow.control.addOnChange { arrow.adjustArrow(to.size) }
        arrow.adjustArrow(to.size)
        group.setOnMousePressed {
            if (it.isPrimaryButtonDown) toggleGroupProperty.get().selectToggle(this)
        }
    }

    fun isBent() = arrow.isBentProperty.get()
    fun bend(toRight: Boolean = true) = arrow.bend(toRight)

    override fun getDrawable(): Node = this.group

    override fun getUserData(): Any = group.userData

    override fun setUserData(p0: Any?) {
        group.userData = p0
    }

    override fun getProperties(): ObservableMap<Any, Any> = group.properties
}