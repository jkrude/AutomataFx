package com.jkrude.automata.shapes

import com.jkrude.automata.logic.State
import com.jkrude.automata.logic.Transition
import com.jkrude.common.Values
import com.jkrude.common.bindLayout
import com.jkrude.common.shapes.DefaultEdgeView
import com.jkrude.common.shapes.VertexView
import javafx.scene.control.Label
import javafx.scene.control.ToggleGroup
import javafx.scene.text.Font
import javafx.scene.text.FontWeight

class LabeledEdge(from: VertexView<State>, to: VertexView<State>, edgeLogic: Transition, toggleGroup: ToggleGroup) :
    DefaultEdgeView<State, Transition>(from, to, edgeLogic, toggleGroup) {

    private val label = Label(edgeLogic.symbol)

    init {
        label.bindLayout(super.arrow.control)
        this.label.font = Font.font("Regular", FontWeight.BOLD, 16.0)
        this.label.textFill = Values.edgeColor
        super.group.children.add(label)
        label.textProperty().addListener { _ ->
            if (label.text.all { it.isLetter() })
                edgeLogic.symbol = label.text
        }
    }
}