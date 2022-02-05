package com.jkrude.automata

import com.jkrude.automata.logic.State
import com.jkrude.automata.logic.Transition
import com.jkrude.automata.shapes.LabeledEdge
import com.jkrude.automata.shapes.StartEdge
import com.jkrude.automata.shapes.StateView
import com.jkrude.common.*
import com.jkrude.common.shapes.Arrow
import com.jkrude.common.shapes.EdgeView
import com.jkrude.common.shapes.VertexView
import javafx.event.EventHandler
import javafx.scene.control.Toggle
import javafx.scene.input.*
import java.util.*

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
            } else {
                val newEdgeCreator = NewEdgeCreator(targets.first())
                centerPane.setOnMouseReleased(newEdgeCreator::onMouseReleased)
                centerPane.setOnMouseDragged(newEdgeCreator::onDragged)
                super.edgeCreator = newEdgeCreator
            }

        }
    }
    override val onKeyPressed: EventHandler<KeyEvent> = EventHandler { event ->
        if (event.code == KeyCode.DELETE && toggleGroup.selectedToggleProperty().get() != null) {
            when (val selected: Toggle = toggleGroup.selectedToggleProperty().get()) {
                is VertexView<*> -> {
                    transitions.removeIf { it.from == selected || it.to == selected }
                    states.remove(selected as VertexView<*>)
                    if(startEdge?.initialState == selected){
                        startEdge = null
                    }
                }
                is EdgeView<*, *> -> transitions.remove(selected as EdgeView<*, *>)
                is StartEdge -> startEdge = null
                else -> throw IllegalStateException("$selected is neither state nor transition")
            }
        }
    }

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


}