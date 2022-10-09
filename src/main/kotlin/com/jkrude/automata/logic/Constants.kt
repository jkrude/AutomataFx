package com.jkrude.automata.logic

object Constants {

    object EmptyWord {
        override fun toString(): String {
            return "ε"
        }

        fun String.isEmptyWord() = this == "ε"
        fun Char.isEmptyWord() = this == 'ε'
    }
}