package com.jkrude.common

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.scene.control.Toggle
import javafx.scene.control.ToggleGroup

interface DefaultToggle : Toggle {

    val isSelected: BooleanProperty
    val toggleGroupProperty: ObjectProperty<ToggleGroup>

    override fun getToggleGroup(): ToggleGroup = toggleGroupProperty.get()
    override fun selectedProperty(): BooleanProperty = isSelected
    override fun setToggleGroup(p0: ToggleGroup) {
        this.toggleGroupProperty.value = toggleGroup
    }

    override fun toggleGroupProperty(): ObjectProperty<ToggleGroup> = this.toggleGroupProperty

    override fun isSelected(): Boolean = this.isSelected.get()

    override fun setSelected(p0: Boolean) {
        this.isSelected.value = p0;
    }
}