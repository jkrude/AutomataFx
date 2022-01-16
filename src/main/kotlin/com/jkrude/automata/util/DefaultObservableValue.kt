package com.jkrude.automata.util

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

interface DefaultObservableValue<T> : ObservableValue<T> {

    val changeListener:  MutableList<ChangeListener<in T>>
    val invalidationListener:  MutableList<InvalidationListener>

    override fun addListener(p0: InvalidationListener) {
        invalidationListener.add(p0)
    }

    override fun removeListener(p0: InvalidationListener) {
        invalidationListener.remove(p0)
    }

    override fun addListener(p0: ChangeListener<in T>) {
        changeListener.add(p0)
    }

    override fun removeListener(p0: ChangeListener<in T>) {
        changeListener.remove(p0)
    }

}