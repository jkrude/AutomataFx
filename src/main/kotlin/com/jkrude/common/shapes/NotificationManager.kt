package com.jkrude.common.shapes

import javafx.animation.PauseTransition
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.scene.control.ListView
import javafx.scene.layout.AnchorPane
import javafx.util.Duration
import java.io.FileNotFoundException
import java.net.URL

class NotificationManager(drawOn: AnchorPane) {

    companion object {
        private const val location = "/com/jkrude/common/shapes/notificationView.css"
        val stylesheet: URL = NotificationManager::class.java.getResource(location)
            ?: throw FileNotFoundException("Did not found stylesheet at : $location")
    }

    enum class Type {
        NEUTRAL,
        POSITIVE,
        NEGATIVE
    }

    private val currentNotifications: ListView<Notification> = ListView()

    init {
        currentNotifications.stylesheets.add(stylesheet.toExternalForm())
        currentNotifications.isFocusTraversable = false
        val notificationHeight = 40.0
        // FIXME workaround: If bound to drawOn height it grows wierd with pop-ups
        // Set height to number of children times their height.
        currentNotifications.prefHeightProperty().bind(
            Bindings.size(currentNotifications.items).multiply(notificationHeight)
        )
        currentNotifications.fixedCellSize = notificationHeight

        AnchorPane.setRightAnchor(currentNotifications, 25.0)
        AnchorPane.setTopAnchor(currentNotifications, 25.0)
        drawOn.children.add(currentNotifications)
    }

    /**
     * Display Notification of message and type for given duration.
     * Removes notification from list if duration is over.
     */
    fun submitNotification(
        message: String,
        displayDuration: Duration = Duration.seconds(5.0),
        type: Type = Type.NEGATIVE
    ) {
        if (!currentlyPresented(message, type)) {
            val notification = Notification(message, type)
            val pauseTransition = PauseTransition(displayDuration)
            notification.setOnClose {
                pauseTransition.stop()
                currentNotifications.items.remove(notification)
            }
            currentNotifications.items.add(notification)
            currentNotifications.toFront()
            pauseTransition.playFromStart()
            pauseTransition.onFinished = EventHandler { currentNotifications.items.remove(notification) }
        }
    }

    /**
     * @return Whether a notification with this message and type is already displayed.
     */
    private fun currentlyPresented(message: String, type: Type): Boolean {
        return currentNotifications.items.any {
            it.message == message && it.type == type
        }
    }

}