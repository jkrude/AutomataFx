package com.jkrude.games

import com.jfoenix.controls.JFXToggleButton
import com.jkrude.common.shapes.Arrow
import com.jkrude.games.logic.Game
import com.jkrude.games.logic.Vertex
import com.jkrude.games.view.CircVertexView
import com.jkrude.games.view.EdgeView
import com.jkrude.games.view.RectVertexView
import com.jkrude.games.view.VertexView
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import java.net.URL
import java.util.*

class Controller : Initializable {

    @FXML
    lateinit var playerSwitch: JFXToggleButton

    @FXML
    lateinit var addStateBtn: Button

    @FXML
    lateinit var confirmBtn: Button

    @FXML
    lateinit var borderPane: BorderPane

    @FXML
    lateinit var centerPane: AnchorPane

    private val states: ObservableList<VertexView<Vertex>> = FXCollections.observableArrayList(ArrayList())
    private val transitionShapes: ObservableList<EdgeView<Vertex>> = FXCollections.observableArrayList(ArrayList())
    private val toggleGroup = ToggleGroup()
    private var edgeCreator: NewEdgeCreator? = null
    private var selectionProcess: SelectionProcess? = null
    private var currentPlayer: Player = Player.ONE
    private var vertexCounter = 1U

    override fun initialize(p0: URL?, p1: ResourceBundle?) {

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
        bindChildrenToStatesAndTransitions()
        createMouseListener()

        addStateBtn.setOnAction {
            createNewVertex(100.0, 100.0)
        }

    }

    private fun bindChildrenToStatesAndTransitions() {
        states.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) centerPane.children.addAll(change.addedSubList.map { it.getDrawable() })
                if (change.wasRemoved()) centerPane.children.removeAll(change.removed.map { it.getDrawable() })
            }
        })
        transitionShapes.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) {
                    for (edge in change.addedSubList) {
                        edge.from.vertexLogic.addEdgeTo(edge.to.vertexLogic)
                        centerPane.children.add(edge.arrow)
                        edge.arrow.toBack()
                    }
                }
                if (change.wasRemoved()) {
                    change.removed.forEach { it.from.vertexLogic.removeEdgeTo(it.to.vertexLogic) }
                    centerPane.children.removeAll(change.removed.map { it.arrow })
                }
            }
        })
    }

    private fun createMouseListener() {
        centerPane.setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY) {
                if (it.clickCount == 2) createNewVertex(it.x, it.y)
                else if (it.target == centerPane) toggleGroup.selectToggle(null)
            }
        }
        centerPane.setOnMousePressed {
            if (it.button == MouseButton.SECONDARY) {
                val targets = states.filter { s -> s.getDrawable().contains(it.x, it.y) }
                if (targets.size != 1) return@setOnMousePressed
                val newEdgeCreator = NewEdgeCreator(targets.first())
                centerPane.setOnMouseReleased(newEdgeCreator::onMouseReleased)
                centerPane.setOnMouseDragged(newEdgeCreator::onDragged)
                this.edgeCreator = newEdgeCreator
            }
        }

        borderPane.setOnKeyPressed { event ->
            if (event.code == KeyCode.DELETE && toggleGroup.selectedToggleProperty().get() != null) {
                when (val selected = toggleGroup.selectedToggleProperty().get()) {
                    is VertexView<*> -> {
                        transitionShapes.removeIf { it.from == selected || it.to == selected }
                        states.remove(selected)
                    }
                    is EdgeView<*> -> transitionShapes.remove(selected)
                    else -> throw IllegalStateException("$selected is neither state nor transition")
                }
            }
        }
    }

    private fun nextID(): UInt {
        return this.vertexCounter.also { this.vertexCounter++ }
    }

    private inner class NewEdgeCreator(val source: VertexView<Vertex>) {
        val arrow = Arrow()
        var finished = false

        init {
            arrow.isVisible = false // only show if dragged too
            arrow.startXProperty.bind(source.xProperty)
            arrow.startYProperty.bind(source.yProperty)
            arrow.endX = source.x
            arrow.endY = source.y
            centerPane.children.add(arrow)
            arrow.toBack()
        }

        fun onDragged(event: MouseEvent) {
            if (event.button != MouseButton.SECONDARY || finished) return
            if (!arrow.isVisible) arrow.isVisible = true
            arrow.endXProperty.set(event.x)
            arrow.endYProperty.set(event.y)
        }

        fun onMouseReleased(event: MouseEvent) {
            if (event.button != MouseButton.SECONDARY || finished) return
            finished = true
            centerPane.children.remove(arrow)
            val targets = this@Controller.states.filter { it.getDrawable().contains(event.x, event.y) }
            if (targets.size != 1 || targets.first() === this.source) return
            val target = targets.first()
            // only one directed edge for each (u,v)
            // TODO Move to automata logic
            if (transitionShapes.none { transition -> transition.from == this.source && transition.to == target }) {
                transitionShapes.add(
                    EdgeView(
                        source,
                        target,
                        arrow
                    )
                )
            }

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
            transitionShapes.forEach { it.arrow.isDisable = true }
            centerPane.addEventFilter(MouseEvent.MOUSE_CLICKED, eventFilter)
            confirmBtn.isDisable = false
            confirmBtn.isVisible = true
            confirmBtn.setOnAction {
                this.endSelection()
            }
        }

        private fun endSelection() {
            states.forEach { it.endSelectionProcess() }
            transitionShapes.forEach { it.arrow.isDisable = false }
            centerPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, eventFilter)
            confirmBtn.isDisable = true
            confirmBtn.isVisible = false
            this@Controller.showAttractor(selected)
        }

    }

    private fun createNewVertex(x: Double, y: Double, player: Player = currentPlayer) {
        val vertex = Vertex(player, nextID().toString());
        val vertexView =
            if (player == Player.ONE) CircVertexView(x to y, toggleGroup, vertex)
            else RectVertexView(x to y, toggleGroup, vertex)
        states.add(vertexView)
        toggleGroup.selectToggle(vertexView)
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
}

