package com.jkrude.common

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import kotlin.reflect.KProperty

class DelegatedProperty<T>(private val parent: ObjectProperty<T>) {
    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return parent.get()
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        parent.value = value
    }
}

class DelegatedDoubleProperty(private val parent: DoubleProperty) {
    operator fun getValue(thisRef: Any, property: KProperty<*>): Double {
        return parent.get()
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: Double) {
        parent.value = value
    }
}

class DelegatedBooleanProperty(private val parent: BooleanProperty) {
    operator fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return parent.get()
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        parent.value = value
    }
}