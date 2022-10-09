package com.jkrude.automata.logic.finite

import com.jkrude.automata.logic.AutomataModel
import com.jkrude.automata.logic.Constants.EmptyWord.isEmptyWord
import com.jkrude.automata.logic.State

class DeterministicFiniteAutomata(
    alphabet: Set<String>,
    stateSet: Set<State>,
    initialState: State
) : AutomataModel(alphabet, stateSet, initialState) {

    init {
        require(stateSet.all {
            it.edges.size == it.edges.toSet().size  // determinism
        })
    }

    fun accepts(word: List<String>): Boolean {
        val path = pathOf(word)
        return path.isNotEmpty() && path.last().isFinal
    }

    fun pathOf(word: List<String>): List<State> {
        require(word.isEmpty()
                || ((word.size == 1) && word.first().isEmptyWord())
                || word.all { it in alphabet })

        val path = mutableListOf(initialState)
        if (word.isEmpty() || ((word.size == 1) && word.first().isEmptyWord())) return path
        var currState = initialState
        for (symbol in word) {
            val options = currState.edges.filter { it.symbol == symbol }
            if (options.size > 1) throw IllegalStateException("Non deterministic automata model")
            if (options.isEmpty()) return path
            path.add(options.first().to)
            currState = options.first().to
        }
        return path
    }
}