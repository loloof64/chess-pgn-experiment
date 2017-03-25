package com.loloof64.chess_core.pieces

abstract class ChessPiece(open val whitePlayer: Boolean) {
    companion object {
        fun fenToPiece(pieceFen: Char): ChessPiece? {
            return when(pieceFen){
                'P' -> Pawn(true)
                'p' -> Pawn(false)
                'N' -> Knight(true)
                'n' -> Knight(false)
                'B' -> Bishop(true)
                'b' -> Bishop(false)
                'R' -> Rook(true)
                'r' -> Rook(false)
                'Q' -> Queen(true)
                'q' -> Queen(false)
                'K' -> King(true)
                'k' -> King(false)
                else -> null
            }
        }
    }
}

data class Pawn(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
data class Knight(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
data class Bishop(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
data class Rook(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
data class Queen(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
data class King(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
