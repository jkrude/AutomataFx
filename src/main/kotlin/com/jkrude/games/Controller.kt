package com.jkrude.games

import com.jfoenix.controls.JFXToggleButton
import com.jkrude.common.DefaultController
import com.jkrude.common.bendIfNecessary
import com.jkrude.common.logic.Edge
import com.jkrude.common.shapes.AbstractVertexView
import com.jkrude.common.shapes.CircleView
import com.jkrude.common.shapes.DefaultEdgeView
import com.jkrude.common.shapes.VertexView
import com.jkrude.common.x2y
import com.jkrude.games.logic.Game
import com.jkrude.games.logic.Vertex
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.input.MouseEvent
import java.net.URL
import java.util.*

private typealias VView = AbstractVertexView<Vertex, *>
private typealias EView = DefaultEdgeView<Vertex, Edge<Vertex>>
private typealias VEdge = Edge<Vertex>

class Controller :
    DefaultController<Vertex, VView, VEdge, EView>() {

    @FXML
    lateinit var playerSwitch: JFXToggleButton

    @FXML
    lateinit var addStateBtn: Button

    @FXML
    lateinit var confirmBtn: Button

    private var selectionProcess: SelectionProcess? = null
    private var currentPlayer: Player = Player.ONE
    private var idSeq = generateSequence(0) { it + 1 }.iterator()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        super.initialize(p0, p1)
        playerSwitch.setOnAction {
            if (playerSwitch.isSelected) {
                playerSwitch.text = "Player One"
                currentPlayer = Player.ONE
            } else {
                playerSwitch.text = "Player Two"
                currentPlayer = Player.TWO
            }
        }
        playerSwitch.isSelected = true
        addStateBtn.setOnAction {
            states.add(createNewVertex(100.0, 100.0))
        }
    }

    private inner class SelectionProcess() {

        val selected: MutableSet<VertexView<Vertex>> = HashSet()
        val eventFilter = EventHandler<MouseEvent> {
            val sel = states.filter { s -> s.getDrawable().contains(it.x, it.y) }
            sel.forEach { s -> s.setMarked() }
            selected += sel
            it.consume()
        }

        init {
            toggleGroup.selectToggle(null)
            states.forEach { it.startSelectionProcess() }
            //transitions.forEach { it.arrow.isDisable = true }
            centerPane.addEventFilter(MouseEvent.MOUSE_CLICKED, eventFilter)
            confirmBtn.isDisable = false
            confirmBtn.isVisible = true
            confirmBtn.setOnAction {
                this.endSelection()
            }
        }

        private fun endSelection() {
            states.forEach { it.endSelectionProcess() }
            //transitions.forEach { it.arrow.isDisable = false }
            centerPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, eventFilter)
            confirmBtn.isDisable = true
            confirmBtn.isVisible = false
            this@Controller.showAttractor(selected)
        }

    }

    override fun createNewVertex(x: Double, y: Double): VView {
        val vertex = Vertex(currentPlayer, idSeq.next().toString());
        // TODO implement arrow logic for rectangles
        return CircleView(x x2y y, toggleGroup, vertex)
    }

    fun startAttractorProcess() {
        this.selectionProcess = SelectionProcess()
    }

    fun showAttractor(selected: MutableSet<VertexView<Vertex>>) {
        val selVertex = selected.map { it.vertexLogic }
        val game: Game<Vertex> = Game(states.map { it.vertexLogic })

        val attractor = game.attr(currentPlayer, selVertex.toSet())
        states.filter { it.vertexLogic in attractor }.forEach { it.setMarked() }
    }

    override fun onTransitionAdded(edge: EView) {
        edge.from.vertexLogic.addEdgeTo(edge.to.vertexLogic)
        bendIfNecessary(super.transitions, edge)
    }

    override fun onTransitionRemoved(edge: EView) {
        edge.from.vertexLogic.removeEdgeTo(edge.to.vertexLogic)
    }

    override fun createNewTransition(from: VertexView<Vertex>, to: VertexView<Vertex>): EView {
        return EView(from, to, VEdge(from.vertexLogic, to.vertexLogic), toggleGroup)
    }

}

