package com.jkrude.automata.shapes

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXChipView
import com.jfoenix.controls.JFXTextField
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import java.io.FileNotFoundException

class TestWordCard {

    @FXML
    private lateinit var basePane: AnchorPane

    @FXML
    private lateinit var wordInput: JFXTextField


    @FXML
    private lateinit var alphabetChipView: JFXChipView<String>

    @FXML
    private lateinit var cancelBtn: JFXButton

    @FXML
    private lateinit var goBtn: JFXButton

    object TestWordCardBuilder {

        private const val locationString = "/com/jkrude/automata/shapes/TestWordCard.fxml"
        private val location = TestWordCard::class.java.getResource(locationString)
            ?: throw FileNotFoundException("Could not find fxml at $locationString")

        fun display(
            stackPane: StackPane,
            darkenBackground: Boolean = true,
            transitionBasedAlphabet: Set<String>,
            // Gets called when confirm button is clicked. Can confirm a reason why it is not valid.
            isValid: (List<String>, Set<String>) -> Boolean = { _, _ -> true },
            onCancel: () -> Unit = {},
            onConfirm: (List<String>, Set<String>) -> Unit
        ) {
            val opaqueLayer = Region().apply {
                style = "-fx-background-color: lightgray;" +
                        "-fx-opacity: 50%"
            }
            val ds1 = DropShadow().apply {
                offsetX = 4.0
                offsetY = 4.0
                color = Color.GRAY
            }
            val loader = FXMLLoader().apply {
                this.location = TestWordCardBuilder.location
            }
            val basePane: AnchorPane = loader.load()
            basePane.effect = ds1

            fun close() {
                if (darkenBackground) stackPane.children.remove(opaqueLayer)
                stackPane.children.remove(basePane)
            }
            stackPane.addEventHandler(MouseEvent.MOUSE_PRESSED) {
                close()
                onCancel()
            }
            val controller: TestWordCard = loader.getController()
            controller.alphabetChipView.chips.addAll(transitionBasedAlphabet.filter { it.isNotBlank() })
            if (darkenBackground) stackPane.children.add(opaqueLayer)
            controller.goBtn.onAction = EventHandler {
                val alphabet = controller.alphabetChipView.chips.toSet()
                val wordAsStringList = controller.wordInput.text.split(" ")
                val valid: Boolean = isValid(wordAsStringList, alphabet)
                if (valid) {
                    close()
                    onConfirm(wordAsStringList, alphabet)
                }
            }
            controller.cancelBtn.onAction = EventHandler {
                close()
                onCancel()
            }
            stackPane.children.add(basePane)
        }
    }
}