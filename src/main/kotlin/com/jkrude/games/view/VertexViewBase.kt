package com.jkrude.games.view

import com.jkrude.common.DefaultToggle
import com.jkrude.common.Point2D
import com.jkrude.common.Values
import com.jkrude.games.logic.Vertex
import javafx.beans.InvalidationListener
import javafx.beans.property.*
import javafx.collections.ObservableMap
import javafx.event.EventHandler
import javafx.scene.control.ToggleGroup
import javafx.scene.shape.Shape

interface VertexView<V : Vertex> {

    val draggableProperty: BooleanProperty
    var isDraggable: Boolean
    val resizeableProperty: BooleanProperty
    var isResizeable: Boolean
    val vertexLogic: V
    val sizeProperty: DoubleProperty
    val size: Double
    val xProperty: DoubleProperty
    var x: Double
    val yProperty: DoubleProperty
    var y: Double

    fun getIntersection(from: Point2D): Point2D
    fun getAsShape(): Shape
    fun setMarked()
    fun startSelectionProcess()
    fun endSelectionProcess()
}

abstract class AbstractVertexView<V : Vertex, S : Shape>(
    initialPoint: Point2D,
    val shape: S,
    toggleGroup: ToggleGroup,
    override val vertexLogic: V,
    private val minSize: Double = 30.0,
    private val maxSize: Double = 100.0
) : VertexView<V>,
    DefaultToggle {

    override val draggableProperty: BooleanProperty = SimpleBooleanProperty(true)
    override var isDraggable: Boolean
        get() = draggableProperty.get()
        set(value) {
            draggableProperty.set(value)
        }
    override val resizeableProperty: BooleanProperty = SimpleBooleanProperty()
    override var isResizeable: Boolean
        get() = resizeableProperty.get()
        set(value) {
            resizeableProperty.set(value)
        }
    override val isSelected: BooleanProperty = object : SimpleBooleanProperty() {
        override fun invalidated() {
            this@AbstractVertexView.shape.fill = if (this.value) Values.selectedColor else Values.primaryColor
        }
    }
    override val toggleGroupProperty = SimpleObjectProperty(toggleGroup)
    override val sizeProperty: DoubleProperty = SimpleDoubleProperty(minSize)
    override var size: Double
        get() = sizeProperty.get()
        set(value) {
            this.sizeProperty.value = value
        }
    private var isDragged = false
    override val xProperty: DoubleProperty = SimpleDoubleProperty(initialPoint.first)
    override var x
        get() = xProperty.get()
        set(value) {
            xProperty.value = value
        }
    override val yProperty: DoubleProperty = SimpleDoubleProperty(initialPoint.second)
    override var y
        get() = yProperty.get()
        set(value) {
            yProperty.value = value
        }
    protected open val hoverListener = InvalidationListener {
        if (this.shape.isHover) this.shape.fill = Values.markedColor
        else this.shape.fill = Values.primaryColor
    }


    init {
        this.shape.fill = Values.primaryColor
        this.shape.onScroll = EventHandler { scrollEvent ->
            if (!this.isResizeable) return@EventHandler
            this.size += scrollEvent.deltaY * 0.7

            if (this.size < this.minSize) this.size = this.minSize
            if (this.size > this.maxSize) this.size = this.maxSize
        }
        addDragListener()
    }

    override fun getAsShape(): Shape = this.shape

    override fun setMarked() {
        this.shape.fill = Values.markedColor
    }

    override fun startSelectionProcess() {
        this.isDraggable = false
        this.isResizeable = false
        this.shape.hoverProperty().addListener(hoverListener)
    }

    override fun endSelectionProcess() {
        this.isDraggable = true
        this.isResizeable = true
        this.shape.hoverProperty().removeListener(hoverListener)
    }


    private fun addDragListener() {
        shape.setOnMousePressed { event ->
            if (event.isPrimaryButtonDown) {
                isDragged = true
                this.shape.toFront()
                toggleGroupProperty.value.selectToggle(this)
            }
        }
        shape.setOnMouseDragged { event ->
            if (isDragged && this.isDraggable) {
                if (event.x > size) x = event.x
                if (event.y > size) y = event.y
            }
        }
        shape.setOnMouseReleased { isDragged = false }
    }

    override fun getUserData(): Any = this.shape.userData

    override fun setUserData(p0: Any?) {
        this.shape.userData = p0
    }

    override fun getProperties(): ObservableMap<Any, Any> = this.shape.properties

}

