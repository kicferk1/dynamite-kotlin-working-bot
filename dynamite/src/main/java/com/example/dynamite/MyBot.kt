package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move

class MyBot : Bot {
    override fun makeMove(gamestate: Gamestate): Move {
        var myDynamite = 100
        var opponentDynamite = 100
        var myScore = 0
        var opponentScore = 0
        for (round in gamestate.rounds) {
            if (round.p1 == Move.D) {
                myDynamite -= 1
            }
            if (round.p2 == Move.D) {
                opponentDynamite -= 1
            }
            if (moveBeats(round.p1, round.p2)) {
                myScore += 1
            }
            if (moveBeats(round.p2, round.p1)) {
                opponentScore += 1
            }
        }

        if (gamestate.rounds.size < 100) {
            return Move.D
        }

        // Get all possible moves
        var moves = Move.values().toList()

        // Don't use dynamite if we ran out
        if (myDynamite == 0) moves = moves.filter { it != Move.D }

        // Don't use water if opponent has no dynamite
        if (opponentDynamite == 0) moves = moves.filter { it != Move.W }

        // If you have large lead, don't use your dynamite
        if (myScore > opponentScore + opponentDynamite) moves = moves.filter { it != Move.D }

        // If you are losing, don't use water
        if (opponentScore > myScore) moves = moves.filter { it != Move.W }

        // Beat simple repetitive moves
        val opponentsLastMoves = gamestate.rounds.takeLast(3)
        val opponentRepetitive = opponentsLastMoves.isNotEmpty() && opponentsLastMoves.all {it.p2 == opponentsLastMoves.first().p2}
        if (opponentRepetitive) {
            moves = getBeatingMoves(opponentsLastMoves.first().p2)
        }

        // Don't repeat moves
        if (moves.size > 1 && gamestate.rounds.isNotEmpty()) {
            moves = moves.filter { it != gamestate.rounds.last().p1 }
        }

        // Dynamite on draw when you have more dynamite than your opponent
        if (gamestate.rounds.isNotEmpty() && gamestate.rounds.last().p1 == gamestate.rounds.last().p2) {
            if (myDynamite > opponentDynamite && myDynamite > 0) return Move.D
        }

        return randomFromList(moves)
    }

    init {}

    private fun moveBeats(move: Move, moveToBeat: Move): Boolean {
        return when (moveToBeat) {
            Move.D -> move == Move.R || move == Move.P || move == Move.S
            Move.P -> move == Move.R || move == Move.W
            Move.R -> move == Move.S || move == Move.W
            Move.S -> move == Move.P || move == Move.W
            Move.W -> move == Move.D
        }
    }

    private fun getBeatingMoves(moveToBeat: Move): List<Move> {
        return when (moveToBeat) {
            Move.D -> listOf(Move.W)
            Move.P -> listOf(Move.S)
            Move.R -> listOf(Move.P)
            Move.S -> listOf(Move.R)
            Move.W -> listOf(Move.R, Move.P, Move.S)
        }
    }

    private fun randomFromList(moves: List<Move>): Move {
        return moves.shuffled().first()
    }
}