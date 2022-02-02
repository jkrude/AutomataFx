package com.jkrude.common.shapes

import com.jkrude.common.logic.Edge
import com.jkrude.common.logic.LabeledNode
import javafx.scene.Group
import javafx.scene.Node

interface EdgeView<V : LabeledNode, E : Edge<V>> {
    val from: VertexView<V>
    val to: VertexView<V>
    val edgeLogic: E

    fun getDrawable(): Node

}

open class DefaultEdgeView<V : LabeledNode, E : Edge<V>>(
    final override val from: VertexView<V>,
    final override val to: VertexView<V>,
    final override val edgeLogic: E
) : EdgeView<V, E> {

    protected val arrow = Arrow(from.xyProperty, to.xyProperty)
    protected val group = Group(arrow)

    init {
        arrow.start.bind(from.xyProperty)
        arrow.end.bind(to.xyProperty)

        to.xyProperty.addOnChange { arrow.adjustArrow(to.size) }
        to.sizeProperty.addListener { _ -> arrow.adjustArrow(to.size) }
        from.xyProperty.addOnChange { arrow.adjustArrow(to.size) }
        arrow.control.addOnChange { arrow.adjustArrow(to.size) }
        arrow.adjustArrow(to.size)
    }

    fun isBent() = arrow.isBentProperty.get()
    fun bend(toRight: Boolean = true) = arrow.bend(toRight)

    override fun getDrawable(): Node = this.group
}