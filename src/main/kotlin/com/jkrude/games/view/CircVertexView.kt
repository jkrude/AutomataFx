package com.jkrude.games.view

import com.jkrude.common.Point2D
import com.jkrude.common.Values
import com.jkrude.common.distTo
import com.jkrude.games.logic.Vertex
import javafx.beans.InvalidationListener
import javafx.scene.control.ToggleGroup
import javafx.scene.shape.Circle

class CircVertexView(initialPoint: Point2D = 0.0 to 0.0, toggleGroup: ToggleGroup, vertex: Vertex) :
    AbstractVertexView<Vertex, Circle>(initialPoint, Circle(), toggleGroup, vertex) {

    override val hoverListener: InvalidationListener = InvalidationListener {
        if (this.shape.isHover) this.shape.stroke = Values.markedColor
        else this.shape.stroke = null
    }

    init {
        this.shape.centerXProperty().bind(super.xProperty)
        this.shape.centerYProperty().bind(super.yProperty)
        this.shape.radiusProperty().bind(super.sizeProperty)
    }

    override fun getIntersection(from: Point2D): Point2D {
        val fromX = from.first
        val fromY = from.second
        val dist = (fromX to fromY) distTo (this.x to this.y)
        val perR = (dist - this.size - 2) / dist
        return fromX - (fromX - this.x) * perR to fromY - (fromY - this.y) * perR
    }

}