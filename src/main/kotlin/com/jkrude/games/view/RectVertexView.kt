package com.jkrude.games.view

import com.jkrude.common.Values
import com.jkrude.common.x2y
import com.jkrude.games.logic.Vertex
import javafx.beans.InvalidationListener
import javafx.geometry.Point2D
import javafx.scene.control.ToggleGroup
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font

class RectVertexView(initialPoint: Point2D = 0.0 x2y 0.0, toggleGroup: ToggleGroup, vertex: Vertex) :
    AbstractVertexView<Vertex, Rectangle>(
        initialPoint, Rectangle(), toggleGroup,
        vertex,
        60.0,
        200.0
    ) {

    override val hoverListener: InvalidationListener = InvalidationListener {
        if (super.group.isHover) this.shape.stroke = Values.markedColor
        else this.shape.stroke = null
    }

    init {
        this.shape.xProperty().bind(super.xyProperty.xProperty.subtract(this.sizeProperty.divide(2)))
        this.shape.yProperty().bind(super.xyProperty.yProperty.subtract(this.sizeProperty.divide(2)))
        this.shape.widthProperty().bind(super.sizeProperty)
        this.shape.heightProperty().bind(super.sizeProperty)
    }

    override fun applyStyling() {
        super.applyStyling()
        this.sizeProperty.addListener { _ ->
            this.label.font = Font("System Regular", this.size / 2.5)
        }
    }

    override fun getIntersection(from: Point2D): Point2D {
        val x = this.xyProperty.x
        val y = this.xyProperty.y
        val left = (x - this.size / 2)
        val right = (x + this.size / 2)
        val up = (y - this.size / 2)
        val down = (y + this.size / 2)
        val fx = from.x
        val fy = from.y
        val vx = fx - x
        val vy = fy - y
        val ex = if (vx > 0) right else left
        val ey = if (vy > 0) down else up
        if (vx == 0.0) return x x2y ey
        if (vy == 0.0) return ex x2y y
        val tx = (ex - x) / vx
        val ty = (ey - y) / vy
        return if (tx <= ty) ex x2y y + tx * vy
        else x + ty * vx x2y ey

    }

}
