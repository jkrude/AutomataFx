package com.jkrude.common

import com.jkrude.common.logic.Edge
import com.jkrude.common.logic.LabeledNode
import com.jkrude.common.shapes.Arrow
import com.jkrude.common.shapes.DefaultEdgeView
import com.jkrude.common.shapes.EdgeView
import com.jkrude.common.shapes.VertexView
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Toggle
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import java.net.URL
import java.util.*


abstract class DefaultController<
        V : LabeledNode, VView : VertexView<V>,
        E : Edge<V>, EView : EdgeView<V, E>
        > :
    Initializable {


    @FXML
    protected lateinit var notificationPane: AnchorPane

    @FXML
    protected lateinit var stackPane: StackPane

    @FXML
    protected lateinit var borderPane: BorderPane

    @FXML
    protected lateinit var drawingPane: AnchorPane

    protected val states: ObservableList<VView> = FXCollections.observableArrayList(ArrayList())
    protected val transitions: ObservableList<EView> = FXCollections.observableArrayList(ArrayList())
    protected var edgeCreator: NewEdgeCreator? = null
    protected val toggleGroup = ToggleGroup()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        syncChildrenToStatesAndTransitions()
        createEventListener()
    }

    private fun syncChildrenToStatesAndTransitions() {
        states.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) {
                    drawingPane.children.addAll(change.addedSubList.map { it.getDrawable() })
                    change.addedSubList.forEach { onVertexAdded(it) }
                }
                if (change.wasRemoved()) {
                    drawingPane.children.removeAll(change.removed.map { it.getDrawable() })
                    change.removed.forEach { onVertexRemoved(it) }
                }
            }
        })
        transitions.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) {
                    for (edge in change.addedSubList) {
                        drawingPane.children.add(edge.getDrawable())
                        onTransitionAdded(edge)
                    }
                }
                if (change.wasRemoved()) {
                    drawingPane.children.removeAll(change.removed.map { it.getDrawable() })
                    change.removed.forEach { onTransitionRemoved(it) }
                }
            }
        })
    }

    protected open val onMouseClicked = EventHandler<MouseEvent> { event ->
        if (event.button == MouseButton.PRIMARY) {
            if (event.clickCount == 2) states.add(createNewVertex(event.x, event.y))
            else if (event.target == drawingPane) toggleGroup.selectToggle(null)
        }
    }

    protected open val onMousePressed = EventHandler<MouseEvent> { event ->
        if (event.button == MouseButton.SECONDARY) {
            val targets = states.filter { s -> s.getDrawable().contains(event.x, event.y) }
            if (targets.size != 1) return@EventHandler
            val newEdgeCreator = NewEdgeCreator(targets.first())
            drawingPane.setOnMouseReleased(newEdgeCreator::onMouseReleased)
            drawingPane.setOnMouseDragged(newEdgeCreator::onDragged)
            this.edgeCreator = newEdgeCreator
        }
    }
    protected open val onKeyPressed = EventHandler<KeyEvent> { event ->
        if (event.code == KeyCode.DELETE && toggleGroup.selectedToggleProperty().get() != null) {
            toggleGroup.selectedToggleProperty().get()?.let {
                removeSelected(it)
            }
        }
    }

    protected open fun removeSelected(selected: Toggle) {
        when (toggleGroup.selectedToggleProperty().get()) {
            is VertexView<*> -> {
                transitions.removeIf { it.from == selected || it.to == selected }
                states.remove(selected as VertexView<*>)
            }
            is EdgeView<*, *> -> transitions.remove(selected)
            else -> throw IllegalStateException("$selected is neither state nor transition")
        }
    }

    private fun createEventListener() {
        drawingPane.onMouseClicked = onMouseClicked
        drawingPane.onMousePressed = onMousePressed

        borderPane.sceneProperty().isNotNull.addListener { _ ->
            if (borderPane.scene != null) {
                borderPane.scene.addEventFilter(KeyEvent.KEY_PRESSED, onKeyPressed)
            }
        }
    }

    abstract fun createNewVertex(x: Double, y: Double): VView
    abstract fun createNewTransition(from: VertexView<V>, to: VertexView<V>): EView
    open fun onTransitionAdded(edge: EView) {
        edge.getDrawable().toBack()
        toggleGroup.selectToggle(edge)
    }

    open fun onTransitionRemoved(edge: EView) {}

    open fun onVertexRemoved(vertex: VertexView<V>) {}
    open fun onVertexAdded(vertex: VertexView<V>) {
        toggleGroup.selectToggle(vertex)
    }

    protected open fun isValidEdge(edge: E): Boolean = transitions.none { it.edgeLogic == edge }

    protected inner class NewEdgeCreator(private val source: VertexView<V>) {
        private val arrow = Arrow()
        var finished = false

        init {
            arrow.isVisible = false // only show if dragged too
            arrow.start.bind(source.xyProperty)
            arrow.end.xy = source.xyProperty.xy
            drawingPane.children.add(arrow)
            arrow.toBack()
        }

        fun onDragged(event: MouseEvent) {
            if (event.button != MouseButton.SECONDARY || finished) return
            if (!arrow.isVisible) arrow.isVisible = true
            arrow.end.xy = event.xy
        }

        // TODO use onDragExisted
        fun onMouseReleased(event: MouseEvent) {
            if (event.button != MouseButton.SECONDARY || finished) return
            finished = true
            drawingPane.children.remove(arrow)
            val targets = this@DefaultController.states.filter { it.getDrawable().contains(event.x, event.y) }
            if (targets.size != 1) return // TODO nodes can overlap
            val target = targets.first()
            val newEdge: EView = createNewTransition(source, target)
            if (isValidEdge(newEdge.edgeLogic)) transitions.add(newEdge)
        }
    }
}

fun <V : LabeledNode, E : Edge<V>, EView : DefaultEdgeView<V, E>> bendIfNecessary(
    edges: ObservableList<out EView>,
    edge: EView
) {
    var adjusted = false
    edges.filter { it.to == edge.from && it.from == edge.to && it !== edge }
        .filter { !it.isBent() }
        .forEach { it.bend().also { adjusted = true } }
    if (adjusted) edge.bend()
}
