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

    abstract fun toFEN() : Char
}

data class Pawn(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun toFEN(): Char {
        return if (whitePlayer) 'P' else 'p'
    }
}
data class Knight(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun toFEN(): Char {
        return if (whitePlayer) 'N' else 'n'
    }
}
data class Bishop(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun toFEN(): Char {
        return if (whitePlayer) 'B' else 'b'
    }
}
data class Rook(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun toFEN(): Char {
        return if (whitePlayer) 'R' else 'r'
    }
}
data class Queen(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun toFEN(): Char {
        return if (whitePlayer) 'Q' else 'q'
    }
}
data class King(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun toFEN(): Char {
        return if (whitePlayer) 'K' else 'k'
    }
}
