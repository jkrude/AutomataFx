package com.jkrude.games.view

import com.jkrude.common.Values
import com.jkrude.common.bindCenter
import com.jkrude.common.x2y
import com.jkrude.games.logic.Vertex
import javafx.beans.InvalidationListener
import javafx.geometry.Point2D
import javafx.scene.control.ToggleGroup
import javafx.scene.shape.Circle
import javafx.scene.text.Font

class CircVertexView(initialPoint: Point2D = 0.0 x2y 0.0, toggleGroup: ToggleGroup, vertex: Vertex) :
    AbstractVertexView<Vertex, Circle>(initialPoint, Circle(), toggleGroup, vertex) {

    override val hoverListener: InvalidationListener = InvalidationListener {
        if (super.group.isHover) this.shape.stroke = Values.markedColor
        else this.shape.stroke = null
    }

    init {
        this.shape.bindCenter(super.xyProperty)
        this.shape.radiusProperty().bind(super.sizeProperty)
    }

    override fun applyStyling() {
        super.applyStyling()
        this.sizeProperty.addListener { _ ->
            this.label.font = Font("System Regular", this.size / 1.6)
        }
    }

    override fun getIntersection(from: Point2D): Point2D {
        val fromX = from.x
        val fromY = from.y
        val dist = from.distance(xyProperty.x x2y xyProperty.y)
        val perR = (dist - this.size - 2) / dist
        return fromX - (fromX - xyProperty.x) * perR x2y fromY - (fromY - xyProperty.y * perR)
    }

}