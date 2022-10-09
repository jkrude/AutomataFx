package com.jkrude.common.shapes

import com.jkrude.common.Values
import com.jkrude.common.css
import com.jkrude.common.shapes.NotificationManager.Type
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.SVGPath

class Notification(
    val message: String,
    val type: Type
) : AnchorPane() {

    private val label: Label = Label().apply {
        text = message
        textFill = Color.WHITE
        setLeftAnchor(this, 12.0)
        setTopAnchor(this, 10.0)
        setRightAnchor(this, 36.0)
        setBottomAnchor(this, 10.0)
    }

    private val closePath = SVGPath().apply {
        content =
            "m16.5 33.6 7.5-7.5 7.5 7.5 2.1-2.1-7.5-7.5 7.5-7.5-2.1-2.1-7.5 7.5-7.5-7.5-2.1 2.1 7.5" +
                    " 7.5-7.5 7.5ZM24 44q-4.1 0-7.75-1.575-3.65-1.575-6.375-4.3-2.725-2.725-4.3-6.3" +
                    "75Q4 28.1 4 24q0-4.15 1.575-7.8 1.575-3.65 4.3-6.35 2.725-2.7 6.375-4.275Q19.9" +
                    " 4 24 4q4.15 0 7.8 1.575 3.65 1.575 6.35 4.275 2.7 2.7 4.275 6.35Q44 19.85 44 " +
                    "24q0 4.1-1.575 7.75-1.575 3.65-4.275 6.375t-6.35 4.3Q28.15 44 24 44Zm0-3q7.1 0" +
                    " 12.05-4.975Q41 31.05 41 24q0-7.1-4.95-12.05Q31.1 7 24 7q-7.05 0-12.025 4.95Q7" +
                    " 16.9 7 24q0 7.05 4.975 12.025Q16.95 41 24 41Zm0-17Z"
    }
    private val closePane = Region().apply {
        shape = closePath
        val size = 20.0
        setMaxSize(size, size)
        setMinSize(size, size)
        setPrefSize(size, size)
        setMaxSize(size, size)
        setRightAnchor(this, 8.0)
        setTopAnchor(this, 8.0)
        setBottomAnchor(this, 8.0)
        style = "-fx-background-color: ${Color.WHITE.css}"
    }

    init {
        val backgroundColor = when (type) {
            Type.NEUTRAL -> Values.darkerBackgroundColor
            Type.POSITIVE -> Values.positiveColor
            Type.NEGATIVE -> Values.negativeColor
        }
        // Rounded corners for left side
        background = Background(BackgroundFill(backgroundColor, CornerRadii(8.0, false), null))
        this.children.addAll(label, closePane)
    }

    fun setOnClose(handler: EventHandler<in MouseEvent>) {
        closePane.onMouseClicked = handler
    }

    // Equal if message and type are equal.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Notification

        if (message != other.message) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }


}