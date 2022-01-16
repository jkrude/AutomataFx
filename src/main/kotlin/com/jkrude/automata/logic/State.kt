package com.jkrude.automata.logic

open class State(val name: String, var isFinal: Boolean, val transitions: MutableList<Transition>) {

    override fun equals(other: Any?): Boolean {
        return other is State && other.name == this.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}