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
import java.net.URL
import java.util.*


abstract class DefaultController<
        V : LabeledNode, VView : VertexView<V>,
        E : Edge<V>, EView : EdgeView<V, E>
        > :
        Initializable {


    @FXML
    protected lateinit var borderPane: BorderPane

    @FXML
    protected lateinit var centerPane: AnchorPane

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
                if (change.wasAdded()) centerPane.children.addAll(change.addedSubList.map { it.getDrawable() })
                if (change.wasRemoved()) centerPane.children.removeAll(change.removed.map { it.getDrawable() })
            }
        })
        transitions.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) {
                    for (edge in change.addedSubList) {
                        onTransitionAdded(edge)
                        centerPane.children.add(edge.getDrawable())
                        edge.getDrawable().toBack()
                    }
                }
                if (change.wasRemoved()) {
                    change.removed.forEach { onTransitionRemoved(it) }
                    centerPane.children.removeAll(change.removed.map { it.getDrawable() })
                }
            }
        })
    }

    protected open val onMouseClicked = EventHandler<MouseEvent> { event ->
        if (event.button == MouseButton.PRIMARY) {
            if (event.clickCount == 2) createNewVertex(event.x, event.y)
            else if (event.target == centerPane) toggleGroup.selectToggle(null)
        }
    }

    protected open val onMousePressed = EventHandler<MouseEvent> { event ->
        if (event.button == MouseButton.SECONDARY) {
            val targets = states.filter { s -> s.getDrawable().contains(event.x, event.y) }
            if (targets.size != 1) return@EventHandler
            val newEdgeCreator = NewEdgeCreator(targets.first())
            centerPane.setOnMouseReleased(newEdgeCreator::onMouseReleased)
            centerPane.setOnMouseDragged(newEdgeCreator::onDragged)
            this.edgeCreator = newEdgeCreator
        }
    }
    protected open val onKeyPressed = EventHandler<KeyEvent> { event ->
        if (event.code == KeyCode.DELETE && toggleGroup.selectedToggleProperty().get() != null) {
            when (val selected: Toggle = toggleGroup.selectedToggleProperty().get()) {
                is VertexView<*> -> {
                    transitions.removeIf { it.from == selected || it.to == selected }
                    states.remove(selected as VertexView<*>)
                }
                is EdgeView<*, *> -> transitions.remove(selected as EdgeView<*, *>)
                else -> throw IllegalStateException("$selected is neither state nor transition")
            }
        }
    }

    private fun createEventListener() {
        centerPane.onMouseClicked = onMouseClicked
        centerPane.onMousePressed = onMousePressed

        borderPane.sceneProperty().isNotNull.addListener { _ ->
            if (borderPane.scene != null) {
                borderPane.scene.onKeyPressed = onKeyPressed
            }
        }
    }

    abstract fun createNewVertex(x: Double, y: Double)
    abstract fun createNewTransition(from: VertexView<V>, to: VertexView<V>): EView
    abstract fun onTransitionAdded(edge: EView)
    abstract fun onTransitionRemoved(edge: EView)
    protected open fun isValidEdge(edge: E): Boolean = transitions.none { it.edgeLogic == edge }

    protected inner class NewEdgeCreator(private val source: VertexView<V>) {
        private val arrow = Arrow()
        var finished = false

        init {
            arrow.isVisible = false // only show if dragged too
            arrow.start.bind(source.xyProperty)
            arrow.end.xy = source.xyProperty.xy
            centerPane.children.add(arrow)
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
            centerPane.children.remove(arrow)
            val targets = this@DefaultController.states.filter { it.getDrawable().contains(event.x, event.y) }
            if (targets.size != 1 || targets.first() === this.source) return // TODO nodes can overlap
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
