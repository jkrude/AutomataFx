package com.jkrude.automata

import com.jkrude.automata.logic.State
import com.jkrude.automata.logic.Transition
import com.jkrude.automata.shapes.LabeledEdge
import com.jkrude.automata.shapes.StateView
import com.jkrude.common.DefaultController
import com.jkrude.common.bendIfNecessary
import com.jkrude.common.shapes.VertexView
import com.jkrude.common.x2y

class Controller : DefaultController<State, StateView, Transition, LabeledEdge>() {

    private val idSeq = generateSequence(0) { it + 1 }.iterator()

    override fun createNewVertex(x: Double, y: Double) {
        val state = State("q${idSeq.next()}")
        val stateView = StateView(x x2y y, super.toggleGroup, state)
        states.add(stateView)
        toggleGroup.selectToggle(stateView)
    }

    override fun createNewTransition(from: VertexView<State>, to: VertexView<State>): LabeledEdge {
        return LabeledEdge(from, to, Transition(from.vertexLogic, to.vertexLogic, ""), toggleGroup)
    }

    override fun onTransitionAdded(edge: LabeledEdge) {
        edge.from.vertexLogic.addEdge(edge.edgeLogic)
        bendIfNecessary(super.transitions, edge)
    }

    override fun onTransitionRemoved(edge: LabeledEdge) {
        edge.from.vertexLogic.removeEdge(edge.edgeLogic)
    }

}