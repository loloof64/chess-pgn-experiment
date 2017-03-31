package com.loloof64.chess_core.game

import com.loloof64.chess_core.pieces.ChessPiece
import com.loloof64.chess_core.pieces.King
import com.loloof64.chess_core.pieces.Rook

class ChessGame(val board: ChessBoard, val info: GameInfo){
    companion object {
        fun fenToGame(fen: String): ChessGame {
            return ChessGame(board = ChessBoard.fenToChessBoard(fen),
                    info = GameInfo.fenToGameInfo(fen))
        }

        val INITIAL_POSITION = ChessGame.fenToGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    }

   fun toFEN(): String = "${board.toFEN()} ${info.toFEN()}"

   fun isValidPseudoLegalMove(startSquare: Pair<Int, Int>,
                              endSquare: Pair<Int, Int>): Boolean {
       val pieceAtStartSquare = board[startSquare.first, startSquare.second]
       if (pieceAtStartSquare == null || pieceAtStartSquare.whitePlayer != info.whiteTurn) return false
       return pieceAtStartSquare.isValidPseudoLegalMove(this, startSquare, endSquare)
   }

    fun isLegalWhiteKingSideCastle(startSquare: Pair<Int, Int>,
                                   endSquare: Pair<Int, Int>): Boolean {
        val pieceAtStartSquare = board[startSquare.first, startSquare.second] ?: return false

        return info.whiteTurn
                && WhiteKingSideCastle in info.castles
                && pieceAtStartSquare == King(whitePlayer = true)
                && board[ChessBoard.RANK_1, ChessBoard.FILE_H] == Rook(whitePlayer = true)
                && startSquare == Pair(ChessBoard.RANK_1, ChessBoard.FILE_E)
                && endSquare == Pair(ChessBoard.RANK_1, ChessBoard.FILE_G)
                && board[ChessBoard.RANK_1, ChessBoard.FILE_F] == null
                && board[ChessBoard.RANK_1, ChessBoard.FILE_G] == null
    }

    fun isLegalWhiteQueenSideCastle(startSquare: Pair<Int, Int>,
                                   endSquare: Pair<Int, Int>): Boolean {
        val pieceAtStartSquare = board[startSquare.first, startSquare.second] ?: return false

        return info.whiteTurn
                && WhiteQueenSideCastle in info.castles
                && pieceAtStartSquare == King(whitePlayer = true)
                && board[ChessBoard.RANK_1, ChessBoard.FILE_A] == Rook(whitePlayer = true)
                && startSquare == Pair(ChessBoard.RANK_1, ChessBoard.FILE_E)
                && endSquare == Pair(ChessBoard.RANK_1, ChessBoard.FILE_C)
                && board[ChessBoard.RANK_1, ChessBoard.FILE_D] == null
                && board[ChessBoard.RANK_1, ChessBoard.FILE_C] == null
                && board[ChessBoard.RANK_1, ChessBoard.FILE_B] == null
    }

    fun isLegalBlackKingSideCastle(startSquare: Pair<Int, Int>,
                                   endSquare: Pair<Int, Int>): Boolean {
        val pieceAtStartSquare = board[startSquare.first, startSquare.second] ?: return false

        return !info.whiteTurn
                && BlackKingSideCastle in info.castles
                && pieceAtStartSquare == King(whitePlayer = false)
                && board[ChessBoard.RANK_8, ChessBoard.FILE_H] == Rook(whitePlayer = false)
                && startSquare == Pair(ChessBoard.RANK_8, ChessBoard.FILE_E)
                && endSquare == Pair(ChessBoard.RANK_8, ChessBoard.FILE_G)
                && board[ChessBoard.RANK_8, ChessBoard.FILE_F] == null
                && board[ChessBoard.RANK_8, ChessBoard.FILE_G] == null
    }

    fun isLegalBlackQueenSideCastle(startSquare: Pair<Int, Int>,
                                    endSquare: Pair<Int, Int>): Boolean {
        val pieceAtStartSquare = board[startSquare.first, startSquare.second] ?: return false

        return !info.whiteTurn
                && BlackQueenSideCastle in info.castles
                && pieceAtStartSquare == King(whitePlayer = false)
                && board[ChessBoard.RANK_8, ChessBoard.FILE_A] == Rook(whitePlayer = false)
                && startSquare == Pair(ChessBoard.RANK_8, ChessBoard.FILE_E)
                && endSquare == Pair(ChessBoard.RANK_8, ChessBoard.FILE_C)
                && board[ChessBoard.RANK_8, ChessBoard.FILE_D] == null
                && board[ChessBoard.RANK_8, ChessBoard.FILE_C] == null
                && board[ChessBoard.RANK_8, ChessBoard.FILE_B] == null
    }

    fun doMove(startSquare: Pair<Int, Int>, endSquare: Pair<Int, Int>): ChessGame {
        val pieceAtStartSquare = board[startSquare.first, startSquare.second] ?: throw NoPieceAtStartCellException()
        if (!pieceAtStartSquare.isValidPseudoLegalMove(this, startSquare, endSquare)) throw IllegalMoveException()

        val modifiedBoardArray = copyBoardIntoArray()
        val newMoveNumber = if (info.whiteTurn) info.moveNumber+1 else info.moveNumber
        var modifiedGameInfo = info.copy(whiteTurn = !info.whiteTurn, moveNumber = newMoveNumber)

        if (isLegalWhiteKingSideCastle(startSquare, endSquare)) {
            val pathEmpty = board[ChessBoard.RANK_1, ChessBoard.FILE_F] == null
                            && board[ChessBoard.RANK_1, ChessBoard.FILE_G] == null
            if (pathEmpty){
                // update king
                modifiedBoardArray[startSquare.first][startSquare.second] = null
                modifiedBoardArray[endSquare.first][endSquare.second] = pieceAtStartSquare

                // update rook
                modifiedBoardArray[ChessBoard.RANK_1][ChessBoard.FILE_H] = null
                modifiedBoardArray[ChessBoard.RANK_1][ChessBoard.FILE_F] = Rook(whitePlayer = true)

                // update game info
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(WhiteKingSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            }
            else throw IllegalMoveException()
        } else if (isLegalWhiteQueenSideCastle(startSquare, endSquare)) {
            val pathEmpty = board[ChessBoard.RANK_1, ChessBoard.FILE_D] == null
                    && board[ChessBoard.RANK_1, ChessBoard.FILE_C] == null
                    && board[ChessBoard.RANK_1, ChessBoard.FILE_B] == null
            if (pathEmpty){
                // update king
                modifiedBoardArray[startSquare.first][startSquare.second] = null
                modifiedBoardArray[endSquare.first][endSquare.second] = pieceAtStartSquare

                // update rook
                modifiedBoardArray[ChessBoard.RANK_1][ChessBoard.FILE_A] = null
                modifiedBoardArray[ChessBoard.RANK_1][ChessBoard.FILE_D] = Rook(whitePlayer = true)

                // update game info
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(WhiteQueenSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            }
            else throw IllegalMoveException()
        } else if (isLegalBlackKingSideCastle(startSquare, endSquare)) {
            val pathEmpty = board[ChessBoard.RANK_8, ChessBoard.FILE_F] == null
                    && board[ChessBoard.RANK_8, ChessBoard.FILE_G] == null
            if (pathEmpty){
                // update king
                modifiedBoardArray[startSquare.first][startSquare.second] = null
                modifiedBoardArray[endSquare.first][endSquare.second] = pieceAtStartSquare

                // update rook
                modifiedBoardArray[ChessBoard.RANK_8][ChessBoard.FILE_H] = null
                modifiedBoardArray[ChessBoard.RANK_8][ChessBoard.FILE_F] = Rook(whitePlayer = false)

                // update game info
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(BlackKingSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            }
            else throw IllegalMoveException()
        } else if (isLegalBlackKingSideCastle(startSquare, endSquare)) {
            val pathEmpty = board[ChessBoard.RANK_8, ChessBoard.FILE_D] == null
                    && board[ChessBoard.RANK_8, ChessBoard.FILE_C] == null
                    && board[ChessBoard.RANK_8, ChessBoard.FILE_B] == null
            if (pathEmpty){
                // update king
                modifiedBoardArray[startSquare.first][startSquare.second] = null
                modifiedBoardArray[endSquare.first][endSquare.second] = pieceAtStartSquare

                // update rook
                modifiedBoardArray[ChessBoard.RANK_8][ChessBoard.FILE_A] = null
                modifiedBoardArray[ChessBoard.RANK_8][ChessBoard.FILE_D] = Rook(whitePlayer = false)

                // update game info
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(BlackQueenSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            }
            else throw IllegalMoveException()
        } else { // regular move
            modifiedBoardArray[startSquare.first][startSquare.second] = null
            modifiedBoardArray[endSquare.first][endSquare.second] = pieceAtStartSquare

            if (pieceAtStartSquare == King(whitePlayer = true)) {
                // update game info
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(WhiteKingSideCastle)
                newCastlesRight.remove(WhiteQueenSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            } else if (pieceAtStartSquare == King(whitePlayer = false)) {
                // update game info
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(BlackKingSideCastle)
                newCastlesRight.remove(BlackQueenSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            } else if (pieceAtStartSquare == Rook(whitePlayer = true)
                    && startSquare == Pair(ChessBoard.RANK_1, ChessBoard.FILE_H)) {
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(WhiteKingSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            } else if (pieceAtStartSquare == Rook(whitePlayer = true)
                    && startSquare == Pair(ChessBoard.RANK_1, ChessBoard.FILE_A)) {
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(WhiteQueenSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            } else if (pieceAtStartSquare == Rook(whitePlayer = false)
                    && startSquare == Pair(ChessBoard.RANK_8, ChessBoard.FILE_H)) {
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(BlackKingSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            } else if (pieceAtStartSquare == Rook(whitePlayer = false)
                    && startSquare == Pair(ChessBoard.RANK_8, ChessBoard.FILE_A)) {
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(BlackQueenSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            }
        }

        val modifiedBoard = ChessBoard(modifiedBoardArray)

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