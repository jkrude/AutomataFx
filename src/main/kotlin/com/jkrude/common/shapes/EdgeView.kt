package com.jkrude.common.shapes

import com.jkrude.common.DefaultToggle
import com.jkrude.common.ReadOnlyPoint2DProperty
import com.jkrude.common.logic.Edge
import com.jkrude.common.logic.LabeledNode
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.ToggleGroup

interface EdgeView<V : LabeledNode, E : Edge<V>> : DefaultToggle {
    val from: VertexView<V>
    val to: VertexView<V>
    val edgeLogic: E
    val group: Group
    val midAnchor: ReadOnlyPoint2DProperty

    fun getDrawable(): Node
    fun isBent(): Boolean
    fun bend(toRight: Boolean = true)

}


open class DefaultEdgeView<V : LabeledNode, E : Edge<V>>(
    final override val from: VertexView<V>,
    final override val to: VertexView<V>,
    final override val edgeLogic: E,
    toggleGroup: ToggleGroup,
    private val edgeImplementation: EdgeView<V, E> =
        if (from == to) SelfLoop(from, edgeLogic, toggleGroup)
        else FromToEdge(from, to, edgeLogic, toggleGroup)
) : EdgeView<V,E> by edgeImplementation
