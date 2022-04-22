package com.jkrude.automata.shapes

import com.jkrude.automata.logic.State
import com.jkrude.automata.logic.Transition
import com.jkrude.common.*
import com.jkrude.common.shapes.DefaultEdgeView
import com.jkrude.common.shapes.VertexView
import javafx.geometry.Insets
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import kotlin.math.ceil

class LabeledEdge(from: VertexView<State>, to: VertexView<State>, edgeLogic: Transition, toggleGroup: ToggleGroup) :
    DefaultEdgeView<State, Transition>(from, to, edgeLogic, toggleGroup) {

    private val textField = TextField(edgeLogic.symbol)
    private var offset = Point2D.ZERO

    init {
        // On creation set textField text = " " for blinking cursor.
        // Note that selection (toggle group) is not equal to focus (nodes).
        fun styling() {
            textField.style = """
            -fx-border-color: none;
            -fx-text-fill: ${Values.primaryColor.css};
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-accent: ${Values.selectedColor.css};
        """.trimIndent()
            // Display textField only if not blank or edge selected
            val background = objectBindingOf(super.selectedProperty()) {
                if (!super.isSelected() && textField.text.isBlank()) {
                    Background(BackgroundFill(Color.TRANSPARENT, CornerRadii(10.0), Insets.EMPTY))
                } else {
                    Background(
                        BackgroundFill(
                            Values.backgroundColor.deriveColor(0.0, 1.0, 0.95, 1.0),
                            CornerRadii(10.0),
                            Insets.EMPTY
                        )
                    )
                }
            }
            textField.backgroundProperty().bind(background)
            textField.alignment = Pos.CENTER
            textField.textProperty().length().addListener { _, _, new ->
                // prefColumnCount always to large -> 0.6 magic number for this font
                textField.prefColumnCount = ceil(new.toDouble() * 0.6).toInt()
            }
        }

        fun positioning() {
            super.midAnchor.addOnChange(::calcPosition)
            calcPosition()
            textField.addEventFilter(MouseEvent.MOUSE_DRAGGED) {
                val mxy = (it.sceneX - 130) x2y it.sceneY  // TODO 130 == size of navigation rail
                val d = mxy.distance(super.midAnchor.xy)
                textField.layout = // restrain to a maximum distance of 100 to the anchor
                    if (d < 100.0) mxy
                    else super.midAnchor.xy - (super.midAnchor.xy - mxy) * (100.0 / d)
                offset = super.midAnchor.xy - mxy
                it.consume()
            }
        }

        fun selection() {
            super.selectedProperty().addListener { _ ->
                if (super.isSelected()) {
                    textField.requestFocus()
                    if (textField.text.isEmpty()) textField.text = " " // -> blinking cursor
                } else {
                    textField.text = textField.text.trim()
                    textField.deselect()
                    if (textField.isFocused) super.group.requestFocus() // avoid cursor by un-focusing
                }
            }
            // TODO Otherwise, FromToEdge or SelfLoop are selected
            super.group.setOnMousePressed {
                super.toggleGroupProperty().get().selectToggle(this)
                it.consume()
            }
            textField.setOnMousePressed { super.toggleGroupProperty.get().selectToggle(this) }
        }

        styling()
        positioning()
        selection()
        textField.textProperty().addListener { _ ->
            if (textField.text.all { it.isLetter() })
                edgeLogic.symbol = textField.text
        }
        super.group.children.add(textField)
    }

    private fun calcPosition() {
        textField.layoutX = super.midAnchor.x - offset.x
        textField.layoutY = super.midAnchor.y - offset.y
    }
}