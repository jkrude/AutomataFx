package com.jkrude.games.view

import com.jkrude.common.shapes.Arrow
import com.jkrude.games.logic.Vertex

open class EdgeView<V : Vertex>(
    val from: VertexView<V>,
    val to: VertexView<V>,
    val arrow: Arrow
) {

    init {
        arrow.startXProperty.bind(from.xProperty)
        arrow.startYProperty.bind(from.yProperty)
        arrow.endXProperty.bind(to.xProperty)
        arrow.endYProperty.bind(to.yProperty)

        to.xProperty.addListener { _, _, _ -> arrow.adjustArrow(to.size) }
        to.yProperty.addListener { _, _, _ -> arrow.adjustArrow(to.size) }
        to.sizeProperty.addListener { _, _, _ -> arrow.adjustArrow(to.size) }
        from.xProperty.addListener { _, _, _ -> arrow.adjustArrow(to.size) }
        from.yProperty.addListener { _, _, _ -> arrow.adjustArrow(to.size) }
        arrow.controlXProperty.addListener { _ -> arrow.adjustArrow(to.size) }
        arrow.controlYProperty.addListener { _ -> arrow.adjustArrow(to.size) }
        arrow.adjustArrow(to.size)
    }

}