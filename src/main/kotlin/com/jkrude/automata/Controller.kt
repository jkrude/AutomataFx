package com.jkrude.automata

import com.jkrude.automata.logic.State
import com.jkrude.automata.logic.Transition
import com.jkrude.automata.shapes.LabeledEdge
import com.jkrude.automata.shapes.StartEdge
import com.jkrude.automata.shapes.StateView
import com.jkrude.common.*
import com.jkrude.common.shapes.Arrow
import com.jkrude.common.shapes.VertexView
import javafx.event.EventHandler
import javafx.scene.control.Toggle
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent

class Controller : DefaultController<State, StateView, Transition, LabeledEdge>() {

    private val idSeq = generateSequence(0) { it + 1 }.iterator()
    private var startEdge: StartEdge? = null
        set(value) {
            if (field != null) centerPane.children.remove(field?.arrow)
            if (value != null) {
                centerPane.children.add(value.arrow)
                value.arrow.toBack()
            }
            field = value
        }

    private var startEdgeCreator: StartEdgeCreator? = null

    override val onMousePressed: EventHandler<MouseEvent> = EventHandler { event ->
        if (event.button == MouseButton.SECONDARY) {
            val targets = states.filter { s -> s.getDrawable().contains(event.x, event.y) }
            if (targets.isEmpty()) {
                val s = StartEdgeCreator(Point2DProperty(event.xy))
                centerPane.setOnMouseReleased(s::onMouseReleased)
                centerPane.setOnMouseDragged(s::onDragged)
                startEdgeCreator = s
            } else super.onMousePressed.handle(event)
        }
    }

    override fun removeSelected(selected: Toggle) {
        if (selected is StartEdge) startEdge = null
        else super.removeSelected(selected)
    }

    override fun createNewVertex(x: Double, y: Double): StateView {
        val state = State("q${idSeq.next()}")
        return StateView(x x2y y, super.toggleGroup, state)
    }

    override fun createNewTransition(from: VertexView<State>, to: VertexView<State>): LabeledEdge {
        return LabeledEdge(from, to, Transition(from.vertexLogic, to.vertexLogic, ""), toggleGroup)
    }

    override fun onVertexRemoved(vertex: VertexView<State>) {
        if (vertex == startEdge?.initialState) startEdge = null
    }

    override fun onTransitionAdded(edge: LabeledEdge) {
        super.onTransitionAdded(edge)
        edge.from.vertexLogic.addEdge(edge.edgeLogic)
        edge.selectedProperty().addListener { _ ->
            // Remove this edge if two edges have the same from - to and the same symbol.
            // In other words keep the model deterministic.
            val edgeLogic = edge.edgeLogic
            if (edgeLogic.from.edges.count { it == edgeLogic } > 1) {
                super.transitions.remove(edge)
            }
        }
        bendIfNecessary(super.transitions, edge)
        bendIfNecessarySameDirection(edge)
    }

    override fun onTransitionRemoved(edge: LabeledEdge) {
        super.onTransitionRemoved(edge)
        edge.from.vertexLogic.removeEdge(edge.edgeLogic)
    }

    private inner class StartEdgeCreator(val start: Point2DProperty) {
        private val arrow: Arrow = Arrow()
        var finished = false

        init {
            this@Controller.startEdge = null
            arrow.start.bind(start)
            arrow.end.xy = start.xy.plus(10.0 x2y 10.0)
            centerPane.children.add(arrow)
            arrow.toBack()
        }

        fun onDragged(event: MouseEvent) {
            if (event.button != MouseButton.SECONDARY || finished) return
            arrow.end.xy = event.xy
            event.consume()
        }

        fun onMouseReleased(event: MouseEvent) {
            if (event.button != MouseButton.SECONDARY || finished) return
            finished = true
            centerPane.children.remove(arrow)
            val targets = this@Controller.states.filter { it.getDrawable().contains(event.x, event.y) }
            if (targets.isEmpty()) return
            startEdge = StartEdge(this.start, targets.first(), this@Controller.toggleGroup)
            event.consume()
        }
    }

    private fun bendIfNecessarySameDirection(newEdge: LabeledEdge) {
        transitions.filter { (it.to == newEdge.to && it.from == newEdge.from) }
            .filter { !it.isBent() }
            .takeIf { it.size > 1 }
            ?.forEachIndexed { idx, edge -> edge.bend(idx % 2 == 0) }
    }

}