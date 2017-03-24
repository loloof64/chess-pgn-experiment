package com.loloof64.chess_core.pieces

abstract class ChessPiece(open val whitePlayer: Boolean)

data class Pawn(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
data class Knight(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
data class Bishop(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
data class Rook(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
data class Queen(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
data class King(override val whitePlayer: Boolean) : ChessPiece(whitePlayer)
