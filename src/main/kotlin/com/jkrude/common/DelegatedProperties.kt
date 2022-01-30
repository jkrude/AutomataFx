package com.jkrude.common

import javafx.beans.property.DoubleProperty
import javafx.beans.property.Property
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


fun <T> asValue(parent: Property<T>): ReadWriteProperty<Any, T> {
    return object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T = parent.value
        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            parent.value = value
        }
    }
}

// Necessary because otherwise number is returned instead of double
fun asValue(parent: DoubleProperty): ReadWriteProperty<Any, Double> {
    return object : ReadWriteProperty<Any, Double> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Double = parent.value
        override fun setValue(thisRef: Any, property: KProperty<*>, value: Double) {
            parent.value = value
        }
    }
}
