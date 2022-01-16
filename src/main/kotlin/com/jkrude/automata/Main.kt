package com.jkrude.automata

import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.SceneAntialiasing
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class Main : Application() {

    override fun start(mainStage: Stage?) {

        val loader = FXMLLoader();
        loader.location = javaClass.getResource("main.fxml")
        val borderPane: BorderPane = loader.load()
        mainStage?.title = "Automata"
        mainStage?.scene = Scene(borderPane, 1280.0, 720.0, false, SceneAntialiasing.BALANCED)
        mainStage?.minHeight = 720.0
        mainStage?.minWidth = 1280.0
        mainStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}
