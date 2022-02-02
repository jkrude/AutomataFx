package com.jkrude.automata.shapes

import com.jkrude.common.bindLayout
import com.jkrude.common.shapes.Arrow
import javafx.scene.control.Label

class LabeledArrow(text: String = "") :
    Arrow() {

    private val label = Label(text)

    init {
        this.children.add(label)
        label.bindLayout(super.control)
    }

}