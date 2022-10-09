package com.jkrude.automata.logic


open class AutomataModel(
    val alphabet: Set<String>,
    val stateSet: Set<State>,
    val initialState: State
) {

    val finalStates: List<State> get() = stateSet.filter { it.isFinal }

    init {
        require(initialState in stateSet)
    }

}
