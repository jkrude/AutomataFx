package com.jkrude.games.logic

import com.jkrude.games.Player

open class Vertex(val player: Player, val id: String, edgesTo: List<Vertex> = ArrayList()) {
    private val edgesInernal: MutableList<Edge> = mutableListOf()
    val edges: List<Edge> get() = edgesInernal

    init {
        edgesInernal.addAll(edgesTo.map { Edge(this, it) })
    }

    fun addEdgeTo(vararg toVertices: Vertex) {
        edgesInernal.addAll(toVertices.map { Edge(this, it) })
    }

    fun removeEdgeTo(vertex: Vertex) {
        edgesInernal.removeIf { it.to == vertex }
    }

    override fun toString(): String = id.toString()
}

open class PVertex(player: Player, id: String, val parity: UInt, edgesTo: List<Vertex> = ArrayList()) : Vertex(
    player, id, edgesTo
)