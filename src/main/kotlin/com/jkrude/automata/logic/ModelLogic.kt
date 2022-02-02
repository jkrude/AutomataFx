package com.jkrude.automata.logic


open class Automata(
    val alphabet: Set<Char>,
    val stateSet: MutableList<State>,
    var initialState: State
) {

    val finalStates: List<State> get() = stateSet.filter { it.isFinal }

    init {
        require(initialState in stateSet)
    }

    fun accepts(word: List<Char>): Boolean {
        val path = pathOf(word)
        return path.isNotEmpty() && path.last().isFinal
    }

    fun pathOf(word: List<Char>): List<State> {
        require(alphabet.containsAll(word))
        val path = mutableListOf(initialState)
        var currState = initialState
        for (sym in word) {
            val options = currState.edges.filter { it.symbol.first() == sym }
            if (options.size > 1) throw IllegalStateException("Non deterministic automata model")
            if (options.isEmpty()) return path
            path.add(options.first().to)
            currState = options.first().to
        }
        return path
    }

}
