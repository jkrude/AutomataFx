package com.jkrude.common.shapes

import com.jkrude.common.DefaultToggle
import com.jkrude.common.Values
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.scene.control.ToggleGroup
import javafx.scene.shape.Circle

open class CircleShape(toggleGroup: ToggleGroup, xy: Pair<Double, Double>, radius: Int = 30) :
    Circle(xy.first, xy.second, radius.toDouble()), DefaultToggle {

    override val toggleGroupProperty: ObjectProperty<ToggleGroup> = SimpleObjectProperty(toggleGroup)

    override val isSelected: BooleanProperty = object : SimpleBooleanProperty() {
        override fun invalidated() {
            fill = if (this.value) Values.selectedColor else Values.primaryColor
        }
    }


    private val dragHandler = DragHandling()

    inner class DragHandling {
        private var active = false

        init {
            setOnMousePressed { event ->
                if (event.isPrimaryButtonDown) {
                    active = true
                    this@CircleShape.toFront()
                    toggleGroupProperty.value.selectToggle(this@CircleShape)
                }
            }
            setOnMouseDragged { event ->
                if (active) {
                    if (event.x > radius) centerX = event.x
                    if (event.y > radius) centerY = event.y
                }
            }
            setOnMouseReleased { active = false }
        }
    }

    init {
        this.fill = Values.primaryColor
        this.onScroll = EventHandler { scrollEvent ->
            this.radius += scrollEvent.deltaY * 0.7
            if (this.radius < 30) this.radius = 30.0
            if (this.radius > 100) this.radius = 100.0
        }
    }
}