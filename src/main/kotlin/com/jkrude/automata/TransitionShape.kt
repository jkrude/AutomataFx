package com.jkrude.automata

import com.jkrude.automata.shapes.Arrow
import com.jkrude.automata.shapes.StateShape
import javafx.beans.property.*
import javafx.collections.ObservableMap
import javafx.scene.control.Toggle
import javafx.scene.control.ToggleGroup
import javafx.scene.paint.Color
import kotlin.math.sqrt

infix fun Pair<Double, Double>.distTo(to: Pair<Double, Double>) =
    sqrt((this.first - to.first) * (this.first - to.first) + (this.second - to.second) * (this.second - to.second))

open class TransitionShape(val from: StateShape, val to: StateShape, val arrow: Arrow, toggleGroup: ToggleGroup) : Toggle {

    protected val shortEndX = SimpleDoubleProperty()
    protected val shortEndY = SimpleDoubleProperty()
    protected val toggleGroupProperty = SimpleObjectProperty(toggleGroup)

    init {
        arrow.startXProperty.bind(from.centerXProperty())
        arrow.startYProperty.bind(from.centerYProperty())
        arrow.endXProperty.bind(shortEndX)
        arrow.endYProperty.bind(shortEndY)
        update(true)

        to.centerXProperty().addListener { _, _, _ -> update() }
        to.centerYProperty().addListener { _, _, _ -> update() }
        to.radiusProperty().addListener { _, _, _ -> update() }
        from.centerYProperty().addListener { _, _, _ -> update() }
        from.centerXProperty().addListener { _, _, _ -> update() }
        arrow.tail.controlXProperty().addListener { _ -> update()}
        arrow.tail.controlYProperty().addListener { _ -> update()}

        val onMousePressed = arrow.tail.onMousePressed ?: null
        arrow.tail.setOnMousePressed {
            onMousePressed?.handle(it)
            if (it.isPrimaryButtonDown) toggleGroupProperty.value.selectToggle(this)
        }
    }

    private fun update(initial: Boolean = false) {
        val fromX = if(initial) from.centerX else arrow.tail.controlX
        val fromY = if(initial) from.centerY else arrow.tail.controlY
        val dist = (fromX to fromY) distTo (to.centerX to to.centerY)
        val perR = (dist - to.radius - 2) / dist
        shortEndX.value = fromX - (fromX - to.centerX) * perR
        shortEndY.value = fromY - (fromY - to.centerY) * perR
    }

    override fun getToggleGroup(): ToggleGroup =
        toggleGroupProperty.get()

    override fun setToggleGroup(value: ToggleGroup?) {
        toggleGroupProperty.value = value
    }

    override fun toggleGroupProperty(): ObjectProperty<ToggleGroup> =
        toggleGroupProperty

    override fun isSelected(): Boolean =
        this.selectedProperty.value

    override fun setSelected(value: Boolean) {
        this.selectedProperty.value = value
    }

    override fun selectedProperty(): BooleanProperty = selectedProperty
    override fun getUserData(): Any = this.arrow.userData


    override fun setUserData(p0: Any?) {
        this.arrow.userData = p0
    }

    override fun getProperties(): ObservableMap<Any, Any> =
        this.arrow.properties


    private val selectedProperty: BooleanProperty = object : BooleanPropertyBase(false) {
        override fun getBean(): Any = this

        override fun getName(): String = "selected"

        override fun invalidated() {
            this@TransitionShape.arrow.tail.stroke = if (this.get()) Color.valueOf("#ffd900") else Color.DARKGRAY
        }

    }

}