package com.jkrude.common.shapes

import com.jkrude.common.DefaultToggle
import com.jkrude.common.Point2DProperty
import com.jkrude.common.Values
import com.jkrude.common.asValue
import com.jkrude.common.logic.LabeledNode
import javafx.beans.InvalidationListener
import javafx.beans.property.*
import javafx.collections.ObservableMap
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ToggleGroup
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import javafx.scene.text.Font

interface VertexView<V : LabeledNode> {

    val draggableProperty: BooleanProperty
    var isDraggable: Boolean
    val resizeableProperty: BooleanProperty
    var isResizeable: Boolean
    val vertexLogic: V
    val sizeProperty: DoubleProperty
    val size: Double
    val xyProperty: Point2DProperty

    fun getIntersection(from: Point2D): Point2D
    fun getDrawable(): Node
    fun setMarked()
    fun startSelectionProcess()
    fun endSelectionProcess()
}

abstract class AbstractVertexView<V : LabeledNode, S : Shape>(
    initialPoint: Point2D,
    val shape: S,
    toggleGroup: ToggleGroup,
    final override val vertexLogic: V,
    private val minSize: Double = 30.0,
    private val maxSize: Double = 100.0
) : VertexView<V>,
    DefaultToggle {

    final override val draggableProperty: BooleanProperty = SimpleBooleanProperty(true)
    override var isDraggable: Boolean by asValue(draggableProperty)

    final override val resizeableProperty: BooleanProperty = SimpleBooleanProperty(true)
    override var isResizeable: Boolean by asValue(resizeableProperty)

    override val isSelected: BooleanProperty = object : SimpleBooleanProperty(false) {
        override fun invalidated() {
            this@AbstractVertexView.shape.fill = if (this.value) Values.selectedColor else Values.primaryColor
        }
    }
    override val toggleGroupProperty = SimpleObjectProperty(toggleGroup)
    final override val sizeProperty: DoubleProperty = SimpleDoubleProperty(minSize)
    override var size: Double by asValue(sizeProperty)

    final override val xyProperty: Point2DProperty = Point2DProperty(initialPoint)

    protected open val hoverListener = InvalidationListener {
        if (this.shape.isHover) this.shape.fill = Values.markedColor
        else this.shape.fill = Values.primaryColor
    }

    protected val label: Label = Label()
    protected val group = Group(shape, label)
    private var isDragged = false

    init {
        this.applyStyling()
        this.group.onScroll = EventHandler { scrollEvent ->
            if (!this.isResizeable) return@EventHandler
            this.size += scrollEvent.deltaY * 0.7

            if (this.size < this.minSize) this.size = this.minSize
            if (this.size > this.maxSize) this.size = this.maxSize
        }
        addDragListener()
    }

    protected open fun applyStyling() {
        this.label.layoutXProperty().bind(xyProperty.xProperty.subtract(this.label.widthProperty().divide(2)))
        this.label.layoutYProperty().bind(xyProperty.yProperty.subtract(this.label.heightProperty().divide(2)))
        this.label.text = vertexLogic.getLabel()
        this.label.font = Font("System Regular", 16.0)
        this.label.textFill = Color.WHITE
        this.shape.fill = Values.primaryColor
    }

    final override fun getDrawable(): Node = this.group

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
        group.setOnMousePressed { event ->
            if (event.isPrimaryButtonDown) {
                isDragged = true
                this.group.toFront()
                toggleGroupProperty.value.selectToggle(this)
            }
        }
        group.setOnMouseDragged { event ->
            if (isDragged && this.isDraggable) {
                if (event.x > size) xyProperty.x = event.x
                if (event.y > size) xyProperty.y = event.y
            }
        }
        group.setOnMouseReleased { isDragged = false }
    }

    override fun getUserData(): Any = this.shape.userData

    override fun setUserData(p0: Any?) {
        this.shape.userData = p0
    }

    override fun getProperties(): ObservableMap<Any, Any> = this.shape.properties

}

