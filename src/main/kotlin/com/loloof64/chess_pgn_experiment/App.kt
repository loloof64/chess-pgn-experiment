package com.loloof64.chess_pgn_experiment

import com.loloof64.chess_core.pieces.*
import com.loloof64.chess_core.game.Game
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
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
    val cellsSize = 50.0
    val picturesSize = 75.0
    val cursorOffset = cellsSize * 0.50
    val picturesScale = cellsSize / picturesSize

    val piecesGroup = Group()
    var game = Game.fenToGame("3r2rk/pbq1np2/1p1ppb1p/8/8/2P2N1P/PP1QBPP1/R4RK1 w - - 0 1")

    var turnComponent: Label? = null
    var currentHighlighter: Label? = null
    var dragStartHighlighter: Label? = null
    var dragStartCoordinates: Pair<Int, Int>? = null
    var movedPieceCursor: ImageView? = null

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
        prefWidth = 9.0*cellsSize
        prefHeight = 9.0*cellsSize
        style {
            backgroundColor += c("#669266")
        }

        addEventFilter(MouseEvent.MOUSE_MOVED, this@ChessBoard::highlightHoveredCell)
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this@ChessBoard::updatePieceCursorLocation)
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this@ChessBoard::highlightHoveredCell)
        addEventFilter(MouseEvent.MOUSE_PRESSED, this@ChessBoard::startPieceDragging)
        addEventFilter(MouseEvent.MOUSE_PRESSED, this@ChessBoard::updatePieceCursorLocation)
        addEventFilter(MouseEvent.MOUSE_RELEASED, this@ChessBoard::endPieceDragging)

        val boardGroup = group {}

        fun addCell(image: String, cellX: Int, cellY: Int) {
            boardGroup.add(imageview(image) {
                scaleX = picturesScale
                scaleY = picturesScale
                layoutX = cellsSize*(cellX.toDouble() + 0.25)
                layoutY = cellsSize*(7.25 - cellY.toDouble())
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
        val font = Font(20.0)
        val color = c("#1200FC")
        val filesCoordinates = "ABCDEFGH"
        (0..7).forEach{ file ->
            val currentCoord = filesCoordinates[file]
            label("$currentCoord"){
                setFont(font)
                layoutX = cellsSize*(0.85+file)
                layoutY = cellsSize * 0.02
                textFill = color
            }
            label("$currentCoord"){
                setFont(font)
                layoutX = cellsSize*(0.85+file)
                layoutY = cellsSize * 8.53
                textFill = color
            }
        }

        val rankCoordinates = "87654321"
        (0..7).forEach { cellLine ->
            val currentCoord = rankCoordinates[cellLine]
            label("$currentCoord"){
                setFont(font)
                layoutX = cellsSize * 0.12
                layoutY = cellsSize*(0.88+cellLine)
                textFill = color
            }
            label("$currentCoord"){
                setFont(font)
                layoutX = cellsSize * 8.70
                layoutY = cellsSize*(0.88+cellLine)
                textFill = color
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
                        layoutX = cellsSize*(file.toDouble() + 0.25)
                        layoutY = cellsSize*(7.25 -rank.toDouble())
                    })
                }
            }
        }

        updatePlayerTurn()
    }

    fun cellCoordinates(evt: MouseEvent): Pair<Int, Int>?{
        val startCoordinate = cellsSize * 0.5
        val endCoordinate = cellsSize * 8.5
        val inBoard = evt.x in (startCoordinate..endCoordinate) && evt.y in (startCoordinate..endCoordinate)

        if (inBoard){
            val cellX = ((evt.x - startCoordinate) / cellsSize).toInt()
            val cellY = ((evt.y - startCoordinate) / cellsSize).toInt()
            return Pair(cellX, cellY)
        }
        else return null
    }

    override fun onDock() {
        super.onDock()
        updatePlayerTurn()
    }

    fun updatePieceCursorLocation(evt: MouseEvent){
        movedPieceCursor?.layoutX = evt.x - cursorOffset
        movedPieceCursor?.layoutY = evt.y - cursorOffset
    }

    fun startPieceDragging(evt: MouseEvent){
        val cellCoords = cellCoordinates(evt)
        if (cellCoords != null){
            // Highlight start cell and records it
            dragStartCoordinates = cellCoords
            dragStartHighlighter = label {
                layoutX = cellsSize * (0.5 + cellCoords.first)
                layoutY = cellsSize * (0.5 + cellCoords.second)
                prefWidth = cellsSize
                prefHeight = cellsSize
                style {
                    backgroundColor += c("#00F")
                    opacity = 0.94
                }
            }


            //Set up custom cursor
            root.cursor = Cursor.NONE
            movedPieceCursor = imageview("chess_bd.png") {
                scaleX = picturesScale
                scaleY = picturesScale
                layoutX = evt.x - cursorOffset
                layoutY = evt.y - cursorOffset
            }
        }
    }

    fun endPieceDragging(evt: MouseEvent) {
        val cellCoords = cellCoordinates(evt)
        if (dragStartHighlighter != null) root.children.remove(dragStartHighlighter)

        root.children.remove(movedPieceCursor)
        movedPieceCursor = null
        root.cursor = Cursor.DEFAULT

        setHighlightedCell(cellCoords)
    }

    fun updatePlayerTurn() {
        if (turnComponent != null) root.children.remove(turnComponent)

        turnComponent = label {
            layoutX = cellsSize * 8.5
            layoutY = cellsSize * 8.5
            prefWidth = cellsSize * 0.5
            prefHeight = cellsSize * 0.5
            style {
                backgroundColor += c(if (game.info.whiteTurn) "#FFF" else "#000")
                opacity = 1.0
            }
        }
    }

    fun highlightHoveredCell(evt: MouseEvent){
        val cellCoords = cellCoordinates(evt)
        val highlightedStatus = if (cellCoords == null) null else cellCoords

        setHighlightedCell(highlightedStatus)
    }

    private fun setHighlightedCell(cellToHighlight: Pair<Int, Int>?) {
        if (currentHighlighter != null) root.children.remove(currentHighlighter)

        if (cellToHighlight != null) {
            currentHighlighter = label {
                layoutX = cellsSize * (0.5 + cellToHighlight.first)
                layoutY = cellsSize * (0.5 + cellToHighlight.second)
                prefWidth = cellsSize
                prefHeight = cellsSize
                style {
                    backgroundColor += c("#22FF44")
                    opacity = 0.6
                }
            }
        }
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