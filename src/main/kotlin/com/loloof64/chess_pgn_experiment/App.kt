package com.loloof64.chess_pgn_experiment

import com.loloof64.chess_core.pieces.*
import com.loloof64.chess_core.game.Game
import javafx.scene.Group
import javafx.scene.control.Hyperlink
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import tornadofx.*

class MyApp: App(MainView::class)

class MainView : View() {
    override val root = borderpane {
        title = "Simple chess game"
        center(ChessBoard::class)
        right = MovesHistory().apply {
            addText("1. ")
            addMoveLink("d4")
            addText(" ")
            addMoveLink("Nf6")

            addText(" 2. ")
            addMoveLink("h3")
            addText(" ")
            addMoveLink("Na6")
        }.root
    }
}

class ChessBoard : View() {
    val cellsSize = 50
    val picturesSize = 75
    val picturesScale = cellsSize.toDouble() / picturesSize

    val piecesGroup = Group()
    var game = Game.fenToGame("3r2rk/pbq1np2/1p1ppb1p/8/8/2P2N1P/PP1QBPP1/R4RK1 w - - 0 1")

    fun pieceToImage(piece: ChessPiece?) : String? {
        return when (piece) {
            Pawn(true) -> "chess_pl.png"
            Pawn(false) -> "chess_pd.png"
            Knight(true) -> "chess_nl.png"
            Knight(false) -> "chess_nd.png"
            Bishop(true) -> "chess_bl.png"
            Bishop(false) -> "chess_bd.png"
            Rook(true) -> "chess_rl.png"
            Rook(false) -> "chess_rd.png"
            Queen(true) -> "chess_ql.png"
            Queen(false) -> "chess_qd.png"
            King(true) -> "chess_kl.png"
            King(false) -> "chess_kd.png"
            else -> null
        }
    }

    fun changePiece(rank: Int, file: Int, newValue: ChessPiece) {
        val pieceView = piecesGroup.lookup("#$rank$file")
        if (pieceView != null) {
            piecesGroup.children.remove(pieceView)
        }
        val image = pieceToImage(newValue)
        if (image != null){
            piecesGroup.add(imageview(image) {
                id = "$rank$file"
                scaleX = picturesScale
                scaleY = picturesScale
                layoutX = cellsSize*(file.toDouble() + 0.5)
                layoutY = cellsSize*(7.5 -rank.toDouble())
            })
        }
    }

    override val root = pane {
        prefWidth = 9.5*cellsSize
        prefHeight = 9.5*cellsSize
        style {
            backgroundColor += c("#669266")
        }

        val boardGroup = group {}

        fun addCell(image: String, cellX: Int, cellY: Int) {
            boardGroup.add(imageview(image) {
                scaleX = picturesScale
                scaleY = picturesScale
                layoutX = cellsSize*(cellX.toDouble() + 0.5)
                layoutY = cellsSize*(7.5 - cellY.toDouble())
            })
        }

        /// adding cells

        for (rank in 0..7){
            for (file in 0..7){
                val image = if ((rank+file) %2 == 0) "wood_dark.png" else "wood_light.png"
                addCell(image, file, rank)
            }
        }

        // adding coordinates
        val font = Font(30.0)
        val color = c("#1200FC")
        val filesCoordinates = "ABCDEFGH"
        (0..7).forEach{ file ->
            val currentCoord = filesCoordinates[file]
            label("$currentCoord"){
                setFont(font)
                layoutX = cellsSize*(1.06+file)
                layoutY = cellsSize * 0.05
                textFill = color
            }
            label("$currentCoord"){
                setFont(font)
                layoutX = cellsSize*(1.06+file)
                layoutY = cellsSize * 8.71
                textFill = color
            }
        }

        val rankCoordinates = "87654321"
        (0..7).forEach { cellLine ->
            val currentCoord = rankCoordinates[cellLine]
            label("$currentCoord"){
                setFont(font)
                layoutX = cellsSize * 0.21
                layoutY = cellsSize*(0.96+cellLine)
                textFill = color
            }
            label("$currentCoord"){
                setFont(font)
                layoutX = cellsSize * 8.90
                layoutY = cellsSize*(0.96+cellLine)
                textFill = color
            }
        }

        // adding player turn indicator
        label(""){
            id = "playerTurn"
            layoutX = cellsSize * 8.8
            layoutY = cellsSize * 8.8
            prefWidth = cellsSize.toDouble() / 2
            prefHeight = cellsSize.toDouble() / 2
            style {
                backgroundColor += c("#FFF")
            }
        }

        // adding pieces

        children.add(piecesGroup)
        for (rank in 0..7){
            for (file in 0..7){
                val piece = game.board[rank, file]
                val image = pieceToImage(piece)
                if (image != null) {
                    piecesGroup.add(imageview(image) {
                        id = "$rank$file"
                        scaleX = picturesScale
                        scaleY = picturesScale
                        layoutX = cellsSize*(file.toDouble() + 0.5)
                        layoutY = cellsSize*(7.5 -rank.toDouble())
                    })
                }
            }
        }

        fun setPlayerTurn(whiteTurn: Boolean) {
            lookup("#playerTurn").style {
                backgroundColor += if (whiteTurn) c("#fff") else c("#000")
            }
        }

        setPlayerTurn(whiteTurn = game.info.whiteTurn)
    }
}

class MovesHistory : View() {
    private var flow : TextFlow by singleAssign()
    override val root = scrollpane {
        flow = textflow {  }
    }

    fun addText(text: String) {
        flow += Text(text)
    }

    fun addMoveLink(text: String) {
        flow += MoveLink(text)
    }
}

class MoveLink(val moveText: String) : Hyperlink(moveText){
    init {
        setOnAction {
            println("$moveText choose !")
        }
    }
}