package com.jkrude.automata.shapes

import javafx.beans.property.BooleanProperty
import javafx.beans.property.BooleanPropertyBase
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.scene.control.Toggle
import javafx.scene.control.ToggleGroup
import javafx.scene.paint.Color
import javafx.scene.shape.Circle

open class StateShape(toggleGroup: ToggleGroup, xy: Pair<Double, Double>, radius: Int = 30) :
    Circle(xy.first, xy.second, radius.toDouble()), Toggle {

    private val toggleGroupProp: ObjectProperty<ToggleGroup> = SimpleObjectProperty(toggleGroup)

    private val backgroundColor = Color.valueOf("#76889a")

    private val dragHandler = DragHandling()

    inner class DragHandling {
        private var active = false

        init {
            setOnMousePressed { event ->
                if (event.isPrimaryButtonDown) {
                    active = true
                    this@StateShape.toFront()
                    toggleGroupProp.value.selectToggle(this@StateShape)
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
        this.fill = backgroundColor
        this.onScroll = EventHandler { scrollEvent ->
            this.radius += scrollEvent.deltaY * 0.7
            if (this.radius < 30) this.radius = 30.0
            if (this.radius > 100) this.radius = 100.0
        }
    }

    // Toggle implementations
    override fun getToggleGroup(): ToggleGroup = toggleGroupProp.get()

    override fun setToggleGroup(toggleGroup: ToggleGroup) {
        this.toggleGroupProp.set(toggleGroup)
    }

    override fun toggleGroupProperty(): ObjectProperty<ToggleGroup> = toggleGroupProp

    override fun isSelected(): Boolean = this.selectedProp.get()

    override fun setSelected(selected: Boolean) {
        this.selectedProp.value = selected
    }

    // Selected property
    override fun selectedProperty(): BooleanProperty = this.selectedProp

    private val selectedProp: BooleanProperty = object : BooleanPropertyBase(false) {
        override fun getBean(): Any = this@StateShape

        override fun getName(): String = "selected"

        override fun invalidated() {
            this@StateShape.fill = if (this.value) Color.valueOf("#ffd900") else backgroundColor
        }

    }

}