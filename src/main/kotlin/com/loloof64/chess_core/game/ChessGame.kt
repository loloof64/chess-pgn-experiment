package com.loloof64.chess_core.game

import com.loloof64.chess_core.pieces.*

class ChessGame(val board: ChessBoard, val info: GameInfo){
    companion object {
        fun fenToGame(fen: String): ChessGame {
            return ChessGame(board = ChessBoard.fenToChessBoard(fen),
                    info = GameInfo.fenToGameInfo(fen))
        }

        val INITIAL_POSITION = ChessGame.fenToGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    }

   fun toFEN(): String = "${board.toFEN()} ${info.toFEN()}"

   fun isValidPseudoLegalMove(startSquare: Coordinates, endSquare: Coordinates): Boolean {
       val pieceAtStartSquare = board[startSquare.rank, startSquare.file]
       if (pieceAtStartSquare?.whitePlayer != info.whiteTurn) return false
       return pieceAtStartSquare.isValidPseudoLegalMove(this, startSquare, endSquare)
   }

    fun isWhiteKingSideCastle(startSquare: Coordinates,
                              endSquare: Coordinates): Boolean {
        val pieceAtStartSquare = board[startSquare.rank, startSquare.file] ?: return false

        return info.whiteTurn
                && WhiteKingSideCastle in info.castles
                && pieceAtStartSquare == King(whitePlayer = true)
                && board[ChessBoard.RANK_1, ChessBoard.FILE_H] == Rook(whitePlayer = true)
                && startSquare == Coordinates(rank = ChessBoard.RANK_1, file = ChessBoard.FILE_E)
                && endSquare == Coordinates(rank = ChessBoard.RANK_1, file = ChessBoard.FILE_G)
                && board[ChessBoard.RANK_1, ChessBoard.FILE_F] == null
                && board[ChessBoard.RANK_1, ChessBoard.FILE_G] == null
    }

    fun isWhiteQueenSideCastle(startSquare: Coordinates,
                               endSquare: Coordinates): Boolean {
        val pieceAtStartSquare = board[startSquare.rank, startSquare.file] ?: return false

        return info.whiteTurn
                && WhiteQueenSideCastle in info.castles
                && pieceAtStartSquare == King(whitePlayer = true)
                && board[ChessBoard.RANK_1, ChessBoard.FILE_A] == Rook(whitePlayer = true)
                && startSquare == Coordinates(rank = ChessBoard.RANK_1, file = ChessBoard.FILE_E)
                && endSquare == Coordinates(rank = ChessBoard.RANK_1, file = ChessBoard.FILE_C)
                && board[ChessBoard.RANK_1, ChessBoard.FILE_D] == null
                && board[ChessBoard.RANK_1, ChessBoard.FILE_C] == null
                && board[ChessBoard.RANK_1, ChessBoard.FILE_B] == null
    }

    fun isBlackKingSideCastle(startSquare: Coordinates,
                              endSquare: Coordinates): Boolean {
        val pieceAtStartSquare = board[startSquare.rank, startSquare.file] ?: return false

        return !info.whiteTurn
                && BlackKingSideCastle in info.castles
                && pieceAtStartSquare == King(whitePlayer = false)
                && board[ChessBoard.RANK_8, ChessBoard.FILE_H] == Rook(whitePlayer = false)
                && startSquare == Coordinates(rank = ChessBoard.RANK_8, file = ChessBoard.FILE_E)
                && endSquare == Coordinates(rank = ChessBoard.RANK_8, file = ChessBoard.FILE_G)
                && board[ChessBoard.RANK_8, ChessBoard.FILE_F] == null
                && board[ChessBoard.RANK_8, ChessBoard.FILE_G] == null
    }

    fun isBlackQueenSideCastle(startSquare: Coordinates,
                               endSquare: Coordinates): Boolean {
        val pieceAtStartSquare = board[startSquare.rank, startSquare.file] ?: return false

        return !info.whiteTurn
                && BlackQueenSideCastle in info.castles
                && pieceAtStartSquare == King(whitePlayer = false)
                && board[ChessBoard.RANK_8, ChessBoard.FILE_A] == Rook(whitePlayer = false)
                && startSquare == Coordinates(rank = ChessBoard.RANK_8, file = ChessBoard.FILE_E)
                && endSquare == Coordinates(rank = ChessBoard.RANK_8, file = ChessBoard.FILE_C)
                && board[ChessBoard.RANK_8, ChessBoard.FILE_D] == null
                && board[ChessBoard.RANK_8, ChessBoard.FILE_C] == null
                && board[ChessBoard.RANK_8, ChessBoard.FILE_B] == null
    }

    fun isEnPassantMove(startSquare: Coordinates, endSquare: Coordinates): Boolean {
        val pieceAtStartSquare = board[startSquare.rank, startSquare.file] ?: return false
        val pieceAtEndSquare = board[endSquare.rank, endSquare.file]
        val isWhiteEnPassantMove = info.whiteTurn && pieceAtStartSquare == Pawn(whitePlayer = true)
            && pieceAtEndSquare == null && info.enPassantFile == endSquare.file
                && startSquare.rank == ChessBoard.RANK_5 && endSquare.rank == ChessBoard.RANK_6
        val isBlackEnPassantMove = !info.whiteTurn && pieceAtStartSquare == Pawn(whitePlayer = false)
                && pieceAtEndSquare == null && info.enPassantFile == endSquare.file
                && startSquare.rank == ChessBoard.RANK_4 && endSquare.rank == ChessBoard.RANK_3

        return isWhiteEnPassantMove || isBlackEnPassantMove
    }

    fun isPromotionMove(startSquare: Coordinates, endSquare: Coordinates): Boolean {
        val pieceAtStartSquare = board[startSquare.rank, startSquare.file] ?: return false

        val promotionAsWhite = info.whiteTurn && pieceAtStartSquare == Pawn(whitePlayer = true)
                                && endSquare.rank == ChessBoard.RANK_8
        val promotionAsBlack = !info.whiteTurn && pieceAtStartSquare == Pawn(whitePlayer = false)
                                && endSquare.rank == ChessBoard.RANK_1

        return promotionAsWhite || promotionAsBlack
    }



    fun doMove(startSquare: Coordinates, endSquare: Coordinates): ChessGame{
        return doMove(startSquare, endSquare, Queen(info.whiteTurn))
    }

    fun doMove(startSquare: Coordinates, endSquare: Coordinates,
               promotionPiece: PromotablePiece): ChessGame {
        val pieceAtStartSquare = board[startSquare.rank, startSquare.file] ?: throw NoPieceAtStartCellException()
        if (!pieceAtStartSquare.isValidPseudoLegalMove(this, startSquare, endSquare)) throw IllegalMoveException()

        val capturingMove = board[endSquare.rank, endSquare.file] != null

        val deltaFile = endSquare.file - startSquare.file
        val deltaRank = endSquare.rank - startSquare.rank

        val modifiedBoardArray = copyBoardIntoArray()
        val newMoveNumber = if (this == INITIAL_POSITION) info.moveNumber else if (info.whiteTurn) info.moveNumber+1 else info.moveNumber
        var modifiedGameInfo = info.copy(whiteTurn = !info.whiteTurn, moveNumber = newMoveNumber)

        if (isEnPassantMove(startSquare, endSquare)) {
            modifiedGameInfo = modifiedGameInfo.copy(enPassantFile = null, nullityCount = 0)
            modifiedBoardArray[startSquare.rank][startSquare.file] = null
            modifiedBoardArray[endSquare.rank][endSquare.file] = pieceAtStartSquare
            modifiedBoardArray[if (info.whiteTurn) (endSquare.rank-1) else (endSquare.rank+1)][endSquare.file] = null
        }
        else if (isPromotionMove(startSquare, endSquare)) {
            modifiedGameInfo = modifiedGameInfo.copy(enPassantFile = null, nullityCount = 0)
            if (promotionPiece.whitePlayer != info.whiteTurn) throw WrongPromotionPieceColor()
            modifiedBoardArray[startSquare.rank][startSquare.file] = null
            modifiedBoardArray[endSquare.rank][endSquare.file] = promotionPiece
        }
        else if (isWhiteKingSideCastle(startSquare, endSquare)) {
            modifiedGameInfo = modifiedGameInfo.copy(enPassantFile = null, nullityCount = modifiedGameInfo.nullityCount+1)
            val pathEmpty = board[ChessBoard.RANK_1, ChessBoard.FILE_F] == null
                            && board[ChessBoard.RANK_1, ChessBoard.FILE_G] == null
            if (pathEmpty){
                // update king
                modifiedBoardArray[startSquare.rank][startSquare.file] = null
                modifiedBoardArray[endSquare.rank][endSquare.file] = pieceAtStartSquare

                // update rook
                modifiedBoardArray[ChessBoard.RANK_1][ChessBoard.FILE_H] = null
                modifiedBoardArray[ChessBoard.RANK_1][ChessBoard.FILE_F] = Rook(whitePlayer = true)

                // update game info
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(WhiteKingSideCastle)
                newCastlesRight.remove(WhiteQueenSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            }
            else throw IllegalMoveException()
        } else if (isWhiteQueenSideCastle(startSquare, endSquare)) {
            modifiedGameInfo = modifiedGameInfo.copy(enPassantFile = null, nullityCount = modifiedGameInfo.nullityCount+1)
            val pathEmpty = board[ChessBoard.RANK_1, ChessBoard.FILE_D] == null
                    && board[ChessBoard.RANK_1, ChessBoard.FILE_C] == null
                    && board[ChessBoard.RANK_1, ChessBoard.FILE_B] == null
            if (pathEmpty){
                // update king
                modifiedBoardArray[startSquare.rank][startSquare.file] = null
                modifiedBoardArray[endSquare.rank][endSquare.file] = pieceAtStartSquare

                // update rook
                modifiedBoardArray[ChessBoard.RANK_1][ChessBoard.FILE_A] = null
                modifiedBoardArray[ChessBoard.RANK_1][ChessBoard.FILE_D] = Rook(whitePlayer = true)

                // update game info
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(WhiteKingSideCastle)
                newCastlesRight.remove(WhiteQueenSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            }
            else throw IllegalMoveException()
        } else if (isBlackKingSideCastle(startSquare, endSquare)) {
            modifiedGameInfo = modifiedGameInfo.copy(enPassantFile = null, nullityCount = modifiedGameInfo.nullityCount+1)
            val pathEmpty = board[ChessBoard.RANK_8, ChessBoard.FILE_F] == null
                    && board[ChessBoard.RANK_8, ChessBoard.FILE_G] == null
            if (pathEmpty){
                // update king
                modifiedBoardArray[startSquare.rank][startSquare.file] = null
                modifiedBoardArray[endSquare.rank][endSquare.file] = pieceAtStartSquare

                // update rook
                modifiedBoardArray[ChessBoard.RANK_8][ChessBoard.FILE_H] = null
                modifiedBoardArray[ChessBoard.RANK_8][ChessBoard.FILE_F] = Rook(whitePlayer = false)

                // update game info
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(BlackKingSideCastle)
                newCastlesRight.remove(BlackQueenSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            }
            else throw IllegalMoveException()
        } else if (isBlackQueenSideCastle(startSquare, endSquare)) {
            modifiedGameInfo = modifiedGameInfo.copy(enPassantFile = null, nullityCount = modifiedGameInfo.nullityCount+1)
            val pathEmpty = board[ChessBoard.RANK_8, ChessBoard.FILE_D] == null
                    && board[ChessBoard.RANK_8, ChessBoard.FILE_C] == null
                    && board[ChessBoard.RANK_8, ChessBoard.FILE_B] == null
            if (pathEmpty){
                // update king
                modifiedBoardArray[startSquare.rank][startSquare.file] = null
                modifiedBoardArray[endSquare.rank][endSquare.file] = pieceAtStartSquare

                // update rook
                modifiedBoardArray[ChessBoard.RANK_8][ChessBoard.FILE_A] = null
                modifiedBoardArray[ChessBoard.RANK_8][ChessBoard.FILE_D] = Rook(whitePlayer = false)

                // update game info
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(BlackKingSideCastle)
                newCastlesRight.remove(BlackQueenSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            }
            else throw IllegalMoveException()
        } else { // regular move
            modifiedBoardArray[startSquare.rank][startSquare.file] = null
            modifiedBoardArray[endSquare.rank][endSquare.file] = pieceAtStartSquare

            val isPawnTwoCellsJump = (pieceAtStartSquare == Pawn(whitePlayer = true)
                    && deltaFile == 0 && deltaRank == 2 && startSquare.rank == ChessBoard.RANK_2)
                    || (pieceAtStartSquare == Pawn(whitePlayer = false)
                        && deltaFile == 0 && deltaRank == -2 && startSquare.rank == ChessBoard.RANK_7)

            if (isPawnTwoCellsJump) {
                modifiedGameInfo = modifiedGameInfo.copy(enPassantFile = startSquare.file, nullityCount = 0)
            } else {
                modifiedGameInfo = modifiedGameInfo.copy(enPassantFile = null)
            }

            if (pieceAtStartSquare::class == Pawn::class || capturingMove){
                modifiedGameInfo = modifiedGameInfo.copy(nullityCount = 0)
            } else {
                modifiedGameInfo = modifiedGameInfo.copy(nullityCount = modifiedGameInfo.nullityCount+1)
            }

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
                    && startSquare == Coordinates(rank = ChessBoard.RANK_1, file = ChessBoard.FILE_H)) {
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(WhiteKingSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            } else if (pieceAtStartSquare == Rook(whitePlayer = true)
                    && startSquare == Coordinates(rank = ChessBoard.RANK_1, file = ChessBoard.FILE_A)) {
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(WhiteQueenSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            } else if (pieceAtStartSquare == Rook(whitePlayer = false)
                    && startSquare == Coordinates(rank = ChessBoard.RANK_8, file = ChessBoard.FILE_H)) {
                val newCastlesRight = mutableListOf(*info.castles.toTypedArray())
                newCastlesRight.remove(BlackKingSideCastle)
                modifiedGameInfo = modifiedGameInfo.copy(castles = newCastlesRight)
            } else if (pieceAtStartSquare == Rook(whitePlayer = false)
                    && startSquare == Coordinates(rank = ChessBoard.RANK_8, file = ChessBoard.FILE_A)) {
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
class WrongPromotionPieceColor: Exception()