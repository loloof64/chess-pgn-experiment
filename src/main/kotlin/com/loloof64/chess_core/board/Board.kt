package com.loloof64.chess_core.board

import com.loloof64.chess_core.pieces.ChessPiece

/**
 * pieces - board pieces array - first dimension is rank (index 0 is for rank 1)
 */
class ChessBoard(pieces: Array<Array<ChessPiece?>>) {
    private val _pieces = pieces

    companion object {
        fun fenToPieces(fen: String):Array<Array<ChessPiece?>>{
            val pieces = Array(8, { Array<ChessPiece?>(8, {null})})

            val boardPart = fen.split("""\s+""".toRegex())
            val lines = boardPart[0].split("/").reversed()

            for (rank in 0..7){
                val currentLine = lines[rank]
                var file = 0
                for (currentChar in currentLine){
                    if (currentChar.isDigit()){
                        file += currentChar.toInt() - '0'.toInt()
                    }
                    else {
                        pieces[rank][file++] = ChessPiece.fenToPiece(currentChar)
                    }
                }
            }

            return pieces
        }

        val INITIAL_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }

    constructor(fen: String): this(ChessBoard.fenToPieces(fen))

    operator fun get(rank: Int, file: Int):ChessPiece? = _pieces[rank][file]
}
