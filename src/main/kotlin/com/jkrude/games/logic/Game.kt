package com.jkrude.games.logic

import com.jkrude.games.Player

open class Game<V : Vertex>(val vertices: List<V>) {

    val v0 = vertices.filter { it.player == Player.ONE }
    val v1 = vertices.filter { it.player == Player.TWO }
    val edges = vertices.flatMap { it.edges }

    init {
        require(vertices.all { v ->
            v.edges.all { e ->
                e.from in vertices && e.to in vertices
            }
        })
    }

    fun pre(p: Player, f: Collection<V>): Set<V> {
        val (vx, vy) = if (p == Player.ONE) v0 to v1 else v1 to v0
        return (vx.filter { it.edges.any { e -> e.to in f } } +
                vy.filter { it.edges.all { e -> e.to in f } }).toSet()
    }

    fun attr(p: Player, f: Set<V>): Set<V> {
        println("Player: $p")
        val currA = f.toMutableSet()
        var i = 0
        while (true) {
            val next = pre(p, currA)
            println("It: $i -> ${next - currA}")
            if (currA.containsAll(next) || i > 50) break
            currA += next
            i++
        }
        return currA
    }
}

open class PGame(vertices: List<PVertex>) : Game<PVertex>(vertices) {

    fun recursive(highestP: UInt) {
        require(highestP.toInt() % 2 == 0)
        val g = this
        var i = -1
        do {
            i++
            val f = (g.v0 + g.v1).filter { it.parity == highestP }.toSet()
            var attrF = g.attr(Player.ONE, f)


        } while (true)
    }
}

fun main() {

    val v1 = PVertex(Player.ONE, 1U, 4U - 1U)
    val v2 = PVertex(Player.ONE, 2U, 4U - 2U)
    val v3 = PVertex(Player.ONE, 3U, 4U - 3U)
    val v4 = PVertex(Player.TWO, 4U, 4U - 4U)
    v1.addEdgeTo(v1, v2)
    v2.addEdgeTo(v2, v3)
    v3.addEdgeTo(v3, v4)
    v4.addEdgeTo(v4, v1)
    val g = Game(listOf(v1, v2, v3, v4))
    println(g.attr(Player.ONE, setOf(v4)))
}