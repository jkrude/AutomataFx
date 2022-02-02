package com.jkrude.games.logic

import com.jkrude.common.logic.Edge
import com.jkrude.common.logic.LabeledNode
import com.jkrude.games.Player

typealias VEdge = Edge<Vertex>

open class Vertex(val player: Player, private val id: String, edgesTo: List<Vertex> = ArrayList()) : LabeledNode {
    private val _edges: MutableList<VEdge> = mutableListOf()
    val edges: List<VEdge> get() = _edges

    init {
        _edges.addAll(edgesTo.map { Edge(this, it) })
    }

    fun addEdgeTo(vararg toVertices: Vertex) {
        _edges.addAll(toVertices.map { Edge(this, it) })
    }

    fun removeEdgeTo(vertex: Vertex) {
        _edges.removeIf { it.to == vertex }
    }

    override fun toString(): String = id
    override fun getLabel(): String = id
}

open class PVertex(player: Player, id: String, val parity: UInt, edgesTo: List<Vertex> = ArrayList()) : Vertex(
    player, id, edgesTo
)