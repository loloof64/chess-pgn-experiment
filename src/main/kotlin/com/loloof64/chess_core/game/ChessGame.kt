package com.loloof64.chess_core.game

import com.loloof64.chess_core.pieces.ChessPiece

class ChessGame(val board: ChessBoard, val info: GameInfo){
    companion object {
        fun fenToGame(fen: String): ChessGame {
            return ChessGame(board = ChessBoard.fenToChessBoard(fen),
                    info = GameInfo.fenToGameInfo(fen))
        }
    }

   fun toFEN(): String = "${board.toFEN()} ${info.toFEN()}"

   fun isValidPseudoLegalMove(startSquare: Pair<Int, Int>,
                              endSquare: Pair<Int, Int>): Boolean {
       val pieceAtStartSquare = board[startSquare.first, startSquare.second]
       if (pieceAtStartSquare == null || pieceAtStartSquare.whitePlayer != info.whiteTurn) return false
       return pieceAtStartSquare.isValidPseudoLegalMove(this, startSquare, endSquare)
   }

    fun doMove(startSquare: Pair<Int, Int>, endSquare: Pair<Int, Int>): ChessGame {
        val pieceAtStartSquare = board[startSquare.first, startSquare.second] ?: throw NoPieceAtStartCellException()
        if (!pieceAtStartSquare.isValidPseudoLegalMove(this, startSquare, endSquare)) throw IllegalMoveException()

        val modifiedBoardArray = copyBoardIntoArray()
        modifiedBoardArray[startSquare.first][startSquare.second] = null
        modifiedBoardArray[endSquare.first][endSquare.second] = pieceAtStartSquare
        val modifiedBoard = ChessBoard(modifiedBoardArray)

        val newMoveNumber = if (info.whiteTurn) info.moveNumber+1 else info.moveNumber
        val modifiedGameInfo = info.copy(whiteTurn = !info.whiteTurn, moveNumber = newMoveNumber)

        return ChessGame(modifiedBoard, modifiedGameInfo)
    }

    fun copyBoardIntoArray() : Array<Array<ChessPiece?>> {
        val pieces = Array(8, { Array<ChessPiece?>(8, {null})})

        for (rank in 0..7){
            for (file in 0..7){
                pieces[rank][file] = board[rank, file]
            }
        }

        return pieces
    }
}

class NoPieceAtStartCellException : Exception()
class IllegalMoveException : Exception()