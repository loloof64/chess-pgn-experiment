package com.loloof64.chess_core

import com.loloof64.chess_core.game.*
import com.winterbe.expekt.should
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class GameInfoTest: Spek({
    describe("GameInfo"){
        val gameInfo1 = GameInfo(whiteTurn = false, castles = listOf(WhiteKingSideCastle, BlackQueenSideCastle),
                enPassantFile = ChessBoard.Companion.FILE_C, nullityCount = 15, moveNumber = 6)
        val gameInfo2 = GameInfo(whiteTurn = false, castles = listOf(WhiteKingSideCastle, BlackQueenSideCastle),
                enPassantFile = ChessBoard.Companion.FILE_C, nullityCount = 15, moveNumber = 6)
        val gameInfo3 = gameInfo1.copy(castles = listOf(WhiteQueenSideCastle))
        val gameInfo4 = gameInfo1.copy(whiteTurn = true)
        val gameInfo5 = gameInfo1.copy(castles = listOf())
        val gameInfo6 = gameInfo1.copy(enPassantFile = null)

        it("should be equal to GameInfo with same data") {
            gameInfo1.should.be.equal(gameInfo2)
            gameInfo1.should.not.be.equal(gameInfo3)
        }

        it("should be correctly generated from FEN string"){
            GameInfo.fenToGameInfo("8/8/8/8/8/8/8/8 b Kq c3 15 6").should.equal(gameInfo1)
            GameInfo.fenToGameInfo("8/8/8/8/8/8/8/8 b Q c3 15 6").should.equal(gameInfo3)
            GameInfo.fenToGameInfo("8/8/8/8/8/8/8/8 w Kq c6 15 6").should.equal(gameInfo4)
            GameInfo.fenToGameInfo("8/8/8/8/8/8/8/8 b - c3 15 6").should.equal(gameInfo5)
            GameInfo.fenToGameInfo("8/8/8/8/8/8/8/8 b Kq - 15 6").should.equal(gameInfo6)
        }

        it("should generate correct FEN string"){
            gameInfo1.toFEN().should.equal("b Kq c3 15 6")
            gameInfo3.toFEN().should.equal("b Q c3 15 6")
            gameInfo4.toFEN().should.equal("w Kq c6 15 6")
            gameInfo5.toFEN().should.equal("b - c3 15 6")
            gameInfo6.toFEN().should.equal("b Kq - 15 6")
        }
    }
})
