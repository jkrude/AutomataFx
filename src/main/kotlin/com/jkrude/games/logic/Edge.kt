package com.jkrude.games.logic

open class Edge(val from: Vertex, val to: Vertex)

open class EdgeTo<V : Vertex>(val to: V)