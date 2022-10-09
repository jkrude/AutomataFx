package com.jkrude.automata

import com.jfoenix.controls.JFXButton
import com.jkrude.automata.logic.Constants.EmptyWord.isEmptyWord
import com.jkrude.automata.logic.State
import com.jkrude.automata.logic.Transition
import com.jkrude.automata.logic.finite.DeterministicFiniteAutomata
import com.jkrude.automata.shapes.LabeledEdge
import com.jkrude.automata.shapes.StartEdge
import com.jkrude.automata.shapes.StateView
import com.jkrude.automata.shapes.TestWordCard
import com.jkrude.common.*
import com.jkrude.common.shapes.Arrow
import com.jkrude.common.shapes.NotificationManager
import com.jkrude.common.shapes.VertexView
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Toggle
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import java.net.URL
import java.util.*

class Controller : DefaultController<State, StateView, Transition, LabeledEdge>() {

    @FXML
    lateinit var wordTestBtn: JFXButton

    private val idSeq = generateSequence(0) { it + 1 }.iterator()
    private var startEdge: StartEdge? = null
        set(value) {
            if (field != null) drawingPane.children.remove(field?.arrow)
            if (value != null) {
                drawingPane.children.add(value.arrow)
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
                drawingPane.setOnMouseReleased(s::onMouseReleased)
                drawingPane.setOnMouseDragged(s::onDragged)
                startEdgeCreator = s
            } else super.onMousePressed.handle(event)
        }
    }

    private lateinit var notificationManager: NotificationManager

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        super.initialize(p0, p1)
        notificationManager = NotificationManager(super.notificationPane)
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
            drawingPane.children.add(arrow)
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
            drawingPane.children.remove(arrow)
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

    /**
     * Test if the automaton is fully defined. If not return the reason.
     * @return if it is fully specified and if not an additional reason.
     */
    private fun isFullySpecified(): Pair<Boolean, String?> {
        val reason: String? =
            if (this.startEdge == null) "Start edge missing."
            else if (super.states.isEmpty()) "No states defined."
            else if (super.states.none { it.isFinal }) "No final state."
            else null

        return (reason == null) to reason
    }

    // Logic related methods
    @FXML
    private fun tryWord() {
        // Require fully specified automata
        val (complete, reason) = isFullySpecified()
        if (!complete) {
            notificationManager.submitNotification("Not fully specified. ${idSeq.next()}")
            return
        }

        val stateSet: Set<State> = super.states.map { it.vertexLogic }.toSet()
        val initialState: State = this.startEdge!!.initialState.vertexLogic
        val transitionBasedAlphabet = super.transitions.map { it.edgeLogic.symbol }.toSet()

        fun simulate(word: List<String>, alphabet: Set<String>) {
            val automata = DeterministicFiniteAutomata(
                alphabet = alphabet.toSet(),
                stateSet = stateSet,
                initialState = initialState
            )
            val isAccepted = automata.accepts(word)
            if (isAccepted) notificationManager.submitNotification(
                "Accepted",
                type = NotificationManager.Type.POSITIVE
            )
            else notificationManager.submitNotification(
                "Not accepted",
                type = NotificationManager.Type.NEGATIVE
            )
        }

        fun validateElseNotify(word: List<String>, alphabet: Set<String>): Boolean {
            if (word.isEmpty()) {
                notificationManager.submitNotification("No word entered.")
                return false
            }
            // Allow empty word
            if (word.size == 1 && word.first().isEmptyWord()) return true

            val firstInvalid = word.firstOrNull { it !in alphabet }
            if (firstInvalid != null) {
                notificationManager.submitNotification("Word has to be in alphabet: $firstInvalid")
                return false
            }
            return true
        }

        // FIXME workaround -> dont open pop up if already open
        if (super.stackPane.children.size > 2) return
        TestWordCard.TestWordCardBuilder.display(
            stackPane = super.stackPane,
            transitionBasedAlphabet = transitionBasedAlphabet,
            isValid = ::validateElseNotify,
            onConfirm = ::simulate
        )

    }

}