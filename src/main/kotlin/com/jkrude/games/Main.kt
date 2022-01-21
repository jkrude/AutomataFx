package com.jkrude.games

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.SceneAntialiasing
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

enum class Player {
    ONE,
    TWO
}

class Main : Application() {

    override fun start(mainStage: Stage?) {

        val loader = FXMLLoader();
        loader.location = javaClass.getResource("main.fxml")
        val borderPane: BorderPane = loader.load()
        mainStage?.title = "Games"
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