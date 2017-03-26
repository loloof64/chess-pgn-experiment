package com.loloof64.chess_core

import com.loloof64.chess_core.game.ChessBoard
import com.loloof64.chess_core.pieces.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import com.winterbe.expekt.should

class BoardTest : Spek({
    describe("board")
    {
        val board1 = ChessBoard(arrayOf<Array<ChessPiece?>>(
                arrayOf(Rook(false), null, null, null, null, Rook(false), King(false), null), // rank 8
                arrayOf(Pawn(false), null, Rook(true), null, null, null, null, null),
                arrayOf(null, Pawn(false), null, null, null, null, Pawn(false), Pawn(true)),
                arrayOf(null, null, null, Bishop(false), null, Pawn(false), null, Knight(false)),
                arrayOf(null, null, null, null, null, null, null, null),
                arrayOf(null, Pawn(true), null, null, null, null, Pawn(true), null),
                arrayOf(Pawn(true), Bishop(true), null, null, null, Pawn(true), null, null),
                arrayOf(null, null, null, null, null, Rook(true), King(true), null)
        ).apply { reverse() })

        val board2 = ChessBoard(arrayOf<Array<ChessPiece?>>(
                arrayOf(Rook(false), null, null, Knight(false), null, null, King(false), null), // rank 8
                arrayOf(Pawn(false), null, null, null, null, Queen(false), Pawn(false), Pawn(false)),
                arrayOf(null, Pawn(false), null, null, null, Pawn(false), null, null),
                arrayOf(null, null, null, null, null, null, null, null),
                arrayOf(null, null, null, Bishop(true), null, null, null, null),
                arrayOf(null, Queen(true), null, null, null, null, Pawn(true), null),
                arrayOf(Pawn(true), Pawn(true), null, null, null, Pawn(true), null, Pawn(true)),
                arrayOf(null, null, null, null, Rook(true), null, King(true), null)
        ).apply { reverse() })

        val fen1 = "r4rk1/p1R5/1p4pP/3b1p1n/8/1P4P1/PB3P2/5RK1"
        val fen2 = "r2n2k1/p4qpp/1p3p2/8/3B4/1Q4P1/PP3P1P/4R1K1"

        it("should not equals to other board initialized with different FEN") {
            val board3 = ChessBoard.fenToChessBoard("3r2rk/pbq1np2/1p1ppb1p/8/8/2P2N1P/PP1QBPP1/R4RK1 w - - 0 1")
            val board4 = ChessBoard.fenToChessBoard(ChessBoard.INITIAL_POSITION)

            board3.should.not.equal(board4)
        }

        it("should equals to other board initialized with same FEN") {
            val commonFEN = ChessBoard.INITIAL_POSITION
            val board3 = ChessBoard.fenToChessBoard(commonFEN)
            val board4 = ChessBoard.fenToChessBoard(commonFEN)

            board3.should.equal(board4)
        }

        it("should be initialized correctly from FEN string"){
            ChessBoard.fenToChessBoard(fen1).should.equal(board1)
            ChessBoard.fenToChessBoard(fen2).should.equal(board2)
        }

        it("should generate correct FEN string"){
            board1.toFEN().should.equal(fen1)
            board2.toFEN().should.equal(fen2)
        }
    }
})