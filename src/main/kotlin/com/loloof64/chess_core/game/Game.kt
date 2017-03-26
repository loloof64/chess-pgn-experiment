package com.loloof64.chess_core.game

class Game(val board: ChessBoard, val info: GameInfo){
    companion object {
        fun fenToGame(fen: String): Game {
            return Game(board = ChessBoard.fenToChessBoard(fen),
                    info = GameInfo.fenToGameInfo(fen))
        }
    }

   fun toFEN(): String = "${board.toFEN()} ${info.toFEN()}"
}