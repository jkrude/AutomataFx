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
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCode
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
    private var edgeCreator: NewEdgeCreator? = null
    protected val toggleGroup = ToggleGroup()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        syncChildrenToStatesAndTransitions()
        createMouseListener()
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

    private fun createMouseListener() {
        centerPane.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY) {
                if (it.clickCount == 2) createNewVertex(it.x, it.y)
                else if (it.target == centerPane) toggleGroup.selectToggle(null)
            }
        }
        centerPane.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            if (it.button == MouseButton.SECONDARY) {
                val targets = states.filter { s -> s.getDrawable().contains(it.x, it.y) }
                if (targets.size != 1) return@addEventHandler
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
                        transitions.removeIf { it.from == selected || it.to == selected }
                        states.remove(selected)
                    }
                    is EdgeView<*, *> -> transitions.remove(selected)
                    else -> throw IllegalStateException("$selected is neither state nor transition")
                }
            }
        }

    }

    abstract fun createNewVertex(x: Double, y: Double)
    abstract fun createNewTransition(from: VertexView<V>, to: VertexView<V>): EView
    abstract fun onTransitionAdded(edge: EView)
    abstract fun onTransitionRemoved(edge: EView)
    protected open fun isValidEdge(edge: E): Boolean = transitions.none { it.edgeLogic == edge }

    inner class NewEdgeCreator(private val source: VertexView<V>) {
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
            // only one directed edge for each (u,v)
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
    edges.filter { it.to == edge.from && it !== edge }
        .filter { !it.isBent() }
        .forEach { it.bend().also { adjusted = true } }
    if (adjusted) edge.bend()
}
