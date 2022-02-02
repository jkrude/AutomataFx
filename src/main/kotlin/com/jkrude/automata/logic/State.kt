package com.jkrude.automata.logic

import com.jkrude.common.logic.LabeledNode

open class State(val name: String, var isFinal: Boolean = false) : LabeledNode {

    private val _edges: MutableList<Transition> = ArrayList()
    val edges: List<Transition> get() = _edges

    override fun getLabel(): String = name

    fun addEdge(edge: Transition) {
        _edges.add(edge)
    }

    fun addEdgeTo(to: State, symbol: String) {
        _edges.add(Transition(this, to, symbol))
    }

    fun removeEdge(edge: Transition) {
        _edges.remove(edge)
    }

    fun removeAllEdgesTo(to: State) {
        _edges.removeIf { it.to == to }
    }

    fun removeEdgeTo(to: State, symbol: String) {
        _edges.removeIf { it.to == to && it.symbol == symbol }
    }


    override fun equals(other: Any?): Boolean {
        return other is State && other.name == this.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}