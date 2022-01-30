package com.jkrude.games.view

import com.jkrude.common.shapes.Arrow
import com.jkrude.games.logic.Vertex

open class EdgeView<V : Vertex>(
    val from: VertexView<V>,
    val to: VertexView<V>,
    val arrow: Arrow
) {

    init {
        arrow.start.bind(from.xyProperty)
        arrow.end.bind(to.xyProperty)

        to.xyProperty.addListener { _, _, _ -> arrow.adjustArrow(to.size) }
        to.sizeProperty.addListener { _, _, _ -> arrow.adjustArrow(to.size) }
        from.xyProperty.addListener { _, _, _ -> arrow.adjustArrow(to.size) }
        arrow.control.addListener { _ -> arrow.adjustArrow(to.size) }
        arrow.adjustArrow(to.size)
    }

}