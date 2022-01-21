package com.jkrude.games.view

import com.jkrude.common.shapes.Arrow
import com.jkrude.games.logic.Vertex
import javafx.beans.property.SimpleDoubleProperty

open class EdgeView<V : Vertex>(
    val from: VertexView<V>,
    val to: VertexView<V>,
    val arrow: Arrow
) {

    val shortEndXProperty = SimpleDoubleProperty()
    var shortEndX: Double
        get() = shortEndXProperty.get()
        set(value) {
            shortEndXProperty.set(value)
        }
    val shortEndYProperty = SimpleDoubleProperty()
    var shortEndY: Double
        get() = shortEndYProperty.get()
        set(value) {
            shortEndYProperty.set(value)
        }


    init {
        arrow.startXProperty.bind(from.xProperty)
        arrow.startYProperty.bind(from.yProperty)
        arrow.endXProperty.bind(shortEndXProperty)
        arrow.endYProperty.bind(shortEndYProperty)

        to.xProperty.addListener { _, _, _ -> update() }
        to.yProperty.addListener { _, _, _ -> update() }
        to.sizeProperty.addListener { _, _, _ -> update() }
        from.xProperty.addListener { _, _, _ -> update() }
        from.yProperty.addListener { _, _, _ -> update() }
        //arrow.tail.controlXProperty().addListener { _ -> update() }
        //arrow.tail.controlYProperty().addListener { _ -> update() }
        update(true)
    }

    private fun update(initial: Boolean = false) {
        val from =
            if (initial) this.from.x to this.from.y
            else arrow.startX to arrow.startY
        val interseciton = to.getIntersection(from)
        this.shortEndX = interseciton.first
        this.shortEndY = interseciton.second
    }

}