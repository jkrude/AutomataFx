package com.jkrude.automata.logic

open class Transition(val from: State, val to: State, val symbol: Char) {

    override fun equals(other: Any?): Boolean {
        if(other !is Transition) return false
        return other.from == this.from && other.to == this.to && other.symbol == this.symbol

    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + symbol.hashCode()
        return result
    }
}