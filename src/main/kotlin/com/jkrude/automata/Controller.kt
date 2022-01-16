package com.jkrude.automata

import com.jkrude.automata.shapes.Arrow
import com.jkrude.automata.shapes.StateShape
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import java.net.URL
import java.util.*

class Controller : Initializable {

    @FXML
    lateinit var addStateBtn: Button

    @FXML
    lateinit var borderPane: BorderPane

    @FXML
    lateinit var centerPane: AnchorPane

    private val states: ObservableList<StateShape> = FXCollections.observableArrayList(ArrayList())
    private val transitionShapes: ObservableList<TransitionShape> = FXCollections.observableArrayList(ArrayList())
    private val toggleGroup = ToggleGroup()
    private var draggedArrow: Arrow? = null

    override fun initialize(p0: URL?, p1: ResourceBundle?) {

        val arrow = Arrow(100.0, 100.0, 250.0, 250.0)
        arrow.tail.controlX = 200.0
        arrow.tail.controlY = 100.0
        centerPane.children.add(arrow)
        centerPane.setOnMouseClicked {
            arrow.tail.startX = 300.0
        }


        bindChildrenToStatesAndTransitions()
        //createMouseListener()

        addStateBtn.setOnAction {
            createNewStateCircle(100.0, 100.0)
        }

    }

    private fun bindChildrenToStatesAndTransitions() {
        states.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) centerPane.children.addAll(change.addedSubList)
                if (change.wasRemoved()) centerPane.children.removeAll(change.removed)
            }
        })
        transitionShapes.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) centerPane.children.addAll(change.addedSubList.map { it.arrow })
                if (change.wasRemoved()) centerPane.children.removeAll(change.removed.map { it.arrow })
            }
        })
    }


    private fun createMouseListener() {
        centerPane.setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY) {
                if (it.clickCount == 2) createNewStateCircle(it.x, it.y)
                else if (it.target == centerPane) toggleGroup.selectToggle(null)
            }
        }
        centerPane.setOnMousePressed {
            if (it.button == MouseButton.SECONDARY && it.target is StateShape) {
                val state: StateShape = it.target as StateShape
                draggedArrow = Arrow()
                centerPane.children.add(draggedArrow)
//                draggedArrow?.stroke = Color.DARKGRAY
                draggedArrow?.toBack()
                draggedArrow?.startXProperty?.bind(state.centerXProperty())
                draggedArrow?.startYProperty?.bind(state.centerYProperty())
                draggedArrow?.endXProperty?.value = state.centerX
                draggedArrow?.endYProperty?.value = state.centerY
            }
        }
        centerPane.setOnMouseDragged {
            if (it.button == MouseButton.SECONDARY) {
                draggedArrow?.endXProperty?.value = it.x
                draggedArrow?.endYProperty?.value = it.y
            }
        }
        centerPane.setOnMouseReleased {
            if (it.button == MouseButton.SECONDARY && it.pickResult.intersectedNode is StateShape) {
                val toState = it.pickResult.intersectedNode as StateShape // TODO
                val fromState = it.target as StateShape
                // only one directed edge for each (u,v)
                centerPane.children.remove(draggedArrow)
                // TODO Move to automata logic
                if (transitionShapes.none { transition -> transition.from == fromState && transition.to == toState }) {
                    transitionShapes.add(
                        TransitionShape(
                            fromState,
                            toState,
                            draggedArrow!!,
                            toggleGroup
                        )
                    )
                    draggedArrow?.toBack()
                }
                draggedArrow = null
            } else {
                centerPane.children.remove(draggedArrow)
            }

        }
        borderPane.setOnKeyPressed { event ->
            if (event.code == KeyCode.DELETE && toggleGroup.selectedToggleProperty().get() != null) {
                when (val selected = toggleGroup.selectedToggleProperty().get()) {
                    is StateShape -> {
                        transitionShapes.removeIf { it.from == selected || it.to == selected }
                        states.remove(selected)
                    }
                    is TransitionShape -> transitionShapes.remove(selected)
                    else -> throw IllegalStateException("$selected is neither state nor transition")
                }
            }
        }
    }


    private fun createNewStateCircle(x: Double, y: Double) {
        val circle = StateShape(toggleGroup, x to y)
        states.add(circle)
        toggleGroup.selectToggle(circle)
    }


}

