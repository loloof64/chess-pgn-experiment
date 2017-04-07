package com.loloof64.chess_pgn_experiment

import com.loloof64.chess_core.pieces.*
import com.loloof64.chess_core.game.ChessGame
import com.loloof64.chess_core.game.Coordinates
import com.loloof64.chess_core.game.Move
import com.loloof64.chess_core.history.HistoryNode
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Application
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.util.Duration
import tornadofx.*

data class FenUpdatingEvent(val fen: String) : FXEvent()
data class AddFANToHistory(val fan: String, val historyNode: HistoryNode) : FXEvent()
data class ChangeChessBoardPosition(val historyNode: HistoryNode) : FXEvent()

fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}

class MyApp: App(MainView::class)

class MainView : View() {
    init {
        subscribe<FenUpdatingEvent> {
            fenZone.text = it.fen
        }

        subscribe<AddFANToHistory> {
            historyZone.updateMovesFromRootNode(historyRootNode)
        }

        subscribe<ChangeChessBoardPosition> {
            val currentHistoryNode = it.historyNode
            val currentFEN = currentHistoryNode.relatedPosition.toFEN()
            chessBoard.setHistoryNode(currentHistoryNode)
            fenZone.text = currentFEN
        }
    }

    val historyRootNode = HistoryNode(relatedPosition = ChessGame.INITIAL_POSITION, parentNode = null,
            moveLeadingToThisNodeFAN = null)
    val fenZone = Text(historyRootNode.relatedPosition.toFEN())
    val historyZone = MovesHistory()
    val chessBoard = ChessBoard(historyRootNode)

    override val root = borderpane {
        title = "Simple chess game"
        center = chessBoard.root
        right = historyZone.root
        bottom = fenZone
    }
}

class ChessBoard(startHistoryNode: HistoryNode) : View() {
    private val cellsSize = 50.0
    private val picturesSize = 75.0
    private val cursorOffset = cellsSize * 0.50
    private val picturesScale = cellsSize / picturesSize

    private val piecesGroup = Group()
    private var currentHistoryNode = startHistoryNode

    private var turnComponent: Label? = null
    private var currentHighlighter: Label? = null
    private var dragStartHighlighter: Label? = null
    private var dragStartCoordinates: Coordinates? = null
    private var movedPieceCursor: ImageView? = null

    fun setHistoryNode(node: HistoryNode) {
        currentHistoryNode = node

        piecesGroup.children.clear()
        addAllPieces()

        updatePlayerTurn()
    }

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

    private fun updatePiecesLocations(move: Move){
        var promotionPiece: PromotablePiece = Queen(currentHistoryNode.relatedPosition.info.whiteTurn)
        if (currentHistoryNode.relatedPosition.isPromotionMove(move)) {
            val dialog = PromotionDialog(currentHistoryNode.relatedPosition.info.whiteTurn)
            val result = dialog.showAndWait()
            result.ifPresent { promotionPiece = it }
        }


        // removing piece at destination cell
        val replacedPieceView = piecesGroup.lookup("#${move.to.rank}${move.to.file}")
        if (replacedPieceView != null) {
            piecesGroup.children.remove(replacedPieceView)
        }

        val movedPieceView = piecesGroup.lookup("#${move.from.rank}${move.from.file}")
        if (currentHistoryNode.relatedPosition.isPromotionMove(move)){
            // replacing piece for promotion move
            piecesGroup.children.remove(movedPieceView)

            val promotedPieceView = imageview(pieceToImage(promotionPiece)) {
                id = "${move.to.rank}${move.to.file}"
                layoutX = cellsSize * (0.25 + move.to.file)
                layoutY = cellsSize * (7.25 - move.to.rank)
                scaleX = picturesScale
                scaleY = picturesScale
            }
            piecesGroup.children.add(promotedPieceView)
        }
        else if (movedPieceView != null) {
            // moving piece for other move
            movedPieceView.id = "${move.to.rank}${move.to.file}"
            movedPieceView.layoutX = cellsSize * (0.25 + move.to.file)
            movedPieceView.layoutY = cellsSize * (7.25 - move.to.rank)
        }

        // Special moves addition
        if (currentHistoryNode.relatedPosition.isEnPassantMove(move)) {
            val capturedPawnView = piecesGroup.lookup(
                    "#${if (currentHistoryNode.relatedPosition.info.whiteTurn) (move.to.rank - 1) else (move.to.rank + 1)}${move.to.file}")
            piecesGroup.children.remove(capturedPawnView)
        }
        else if (currentHistoryNode.relatedPosition.isWhiteKingSideCastle(move)) {
            val movedRookView = piecesGroup.lookup("#07")
            movedRookView.id = "05"
            movedRookView.layoutX = cellsSize * 5.25
            movedRookView.layoutY = cellsSize * 7.25
        } else if (currentHistoryNode.relatedPosition.isWhiteQueenSideCastle(move)) {
            val movedRookView = piecesGroup.lookup("#00")
            movedRookView.id = "03"
            movedRookView.layoutX = cellsSize * 3.25
            movedRookView.layoutY = cellsSize * 7.25
        } else if (currentHistoryNode.relatedPosition.isBlackKingSideCastle(move)) {
            val movedRookView = piecesGroup.lookup("#77")
            movedRookView.id = "75"
            movedRookView.layoutX = cellsSize * 5.25
            movedRookView.layoutY = cellsSize * 0.25
        } else if (currentHistoryNode.relatedPosition.isBlackQueenSideCastle(move)) {
            val movedRookView = piecesGroup.lookup("#70")
            movedRookView.id = "73"
            movedRookView.layoutX = cellsSize * 3.25
            movedRookView.layoutY = cellsSize * 0.25
        }

        val moveFAN = currentHistoryNode.relatedPosition.getFANForMove(move = move, promotionPiece = promotionPiece)
        val positionAfterMove = currentHistoryNode.relatedPosition.doMoveWithValidation(move = move, promotionPiece = promotionPiece)
        val historyNodeAfterMove = HistoryNode(parentNode = currentHistoryNode, relatedPosition = positionAfterMove,
                moveLeadingToThisNodeFAN = moveFAN)
        fire(AddFANToHistory(currentHistoryNode.relatedPosition.getFANForMove(move = move, promotionPiece = promotionPiece),
                historyNode = historyNodeAfterMove))
        currentHistoryNode = historyNodeAfterMove

        updatePlayerTurn()
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
        val font = Font("Arial", 20.0)
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
        addAllPieces()

        updatePlayerTurn()
    }

    private fun addAllPieces() {
        for (rank in 0..7) {
            for (file in 0..7) {
                val piece = currentHistoryNode.relatedPosition.board[rank, file]
                val image = pieceToImage(piece)
                if (image != null) {
                    piecesGroup.add(imageview(image) {
                        id = "$rank$file"
                        scaleX = picturesScale
                        scaleY = picturesScale
                        layoutX = cellsSize * (file.toDouble() + 0.25)
                        layoutY = cellsSize * (7.25 - rank.toDouble())
                    })
                }
            }
        }
    }

    private fun cellCoordinates(evt: MouseEvent): Coordinates?{
        val startCoordinate = cellsSize * 0.5
        val cellX = ((evt.x - startCoordinate) / cellsSize).toInt()
        val cellY = 7 - ((evt.y - startCoordinate) / cellsSize).toInt()

        val inBoard = cellX in 0..7 && cellY in 0..7

        if (inBoard){
            return Coordinates(rank = cellY, file = cellX)
        }
        else return null
    }

    override fun onDock() {
        super.onDock()
        updatePlayerTurn()
    }

    private fun updatePieceCursorLocation(evt: MouseEvent){
        movedPieceCursor?.layoutX = evt.x - cursorOffset
        movedPieceCursor?.layoutY = evt.y - cursorOffset
    }

    private fun startPieceDragging(evt: MouseEvent){
        val cellCoords = cellCoordinates(evt)
        if (cellCoords != null) {
            val pieceAtCell = currentHistoryNode.relatedPosition.board[cellCoords.rank, cellCoords.file]
            val weCanStartDnd = pieceAtCell?.whitePlayer == currentHistoryNode.relatedPosition.info.whiteTurn

            if (weCanStartDnd) {
                // Highlight start cell and records it
                dragStartCoordinates = cellCoords
                dragStartHighlighter = label {
                    layoutX = cellsSize * (0.5 + cellCoords.file)
                    layoutY = cellsSize * (7.5 - cellCoords.rank)
                    prefWidth = cellsSize
                    prefHeight = cellsSize
                    style {
                        backgroundColor += c("#00F")
                        opacity = 0.94
                    }
                }


                //Set up custom cursor
                root.cursor = Cursor.NONE
                movedPieceCursor = imageview(pieceToImage(pieceAtCell)) {
                    scaleX = picturesScale
                    scaleY = picturesScale
                    layoutX = evt.x - cursorOffset
                    layoutY = evt.y - cursorOffset
                }
            }
        }
    }

    private fun animatePieceBackToItsOriginCell(originCellCoords: Coordinates?){
        if (dragStartCoordinates != null) {

            // cancel animation
            val animationEndX = cellsSize*(dragStartCoordinates?.file?.toDouble() ?:0.0 + 0.5)
            val animationEndY = cellsSize*(7.5 - (dragStartCoordinates?.rank?.toDouble() ?:0.0))
            val timeline = Timeline()
            timeline.keyFrames.add(
                    KeyFrame(Duration.millis(200.0),
                            KeyValue(movedPieceCursor?.layoutXProperty(), animationEndX),
                            KeyValue(movedPieceCursor?.layoutYProperty(), animationEndY))
            )
            timeline.play()
            timeline.setOnFinished {
                resetDnDStatus(originCellCoords)
            }
        }
    }

    private fun resetDnDStatus(originCellCoords: Coordinates?){
        root.children.remove(movedPieceCursor)

        movedPieceCursor = null
        root.cursor = Cursor.DEFAULT

        setHighlightedCell(originCellCoords)
        if (dragStartHighlighter != null) root.children.remove(dragStartHighlighter)
        dragStartCoordinates = null
    }

    private fun endPieceDragging(evt: MouseEvent) {
        val cellCoords = cellCoordinates(evt)

        if (dragStartCoordinates != null && cellCoords != null &&
                currentHistoryNode.relatedPosition.isValidMove(Move(from = dragStartCoordinates!!, to = cellCoords))){
            validateDnD(cellCoords)
        }
        else {
            animatePieceBackToItsOriginCell(dragStartCoordinates)
        }
    }

    private fun validateDnD(cellCoords: Coordinates?) {
        if (cellCoords != null && dragStartCoordinates != null) {
            val move = Move(from = dragStartCoordinates!!, to = cellCoords)
            updatePiecesLocations(move)
            resetDnDStatus(cellCoords)
            fire(FenUpdatingEvent(currentHistoryNode.relatedPosition.toFEN()))
        }
    }

    private fun updatePlayerTurn() {
        if (turnComponent != null) root.children.remove(turnComponent)

        turnComponent = label {
            layoutX = cellsSize * 8.5
            layoutY = cellsSize * 8.5
            prefWidth = cellsSize * 0.5
            prefHeight = cellsSize * 0.5
            style {
                backgroundColor += c(if (currentHistoryNode.relatedPosition.info.whiteTurn) "#FFF" else "#000")
                opacity = 1.0
            }
        }
    }

    private fun highlightHoveredCell(evt: MouseEvent){
        val cellCoords = cellCoordinates(evt)
        val highlightedStatus = if (cellCoords == null) null else cellCoords

        setHighlightedCell(highlightedStatus)
    }

    private fun setHighlightedCell(cellToHighlight: Coordinates?) {
        if (currentHighlighter != null) root.children.remove(currentHighlighter)

        if (cellToHighlight != null) {
            currentHighlighter = label {
                layoutX = cellsSize * (0.5 + cellToHighlight.file)
                layoutY = cellsSize * (7.5 -  cellToHighlight.rank)
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
    private var tabLevel = 0

    override val root = scrollpane {
        flow = textflow {  }
        prefWidth = 500.0
    }

    private fun addText(text: String) {
        flow += Text(text)
    }

    private fun addMoveLink(text: String, relatedHistoryNode: HistoryNode) {
        flow += MoveLink(text, relatedHistoryNode, this)
    }

    fun updateMovesFromRootNode(rootNode: HistoryNode) {
        flow.clear()
        addAllMovesFromNode(rootNode)
    }

    fun addHeadMove(nodeToAdd: HistoryNode){
        if (!nodeToAdd.relatedPosition.info.whiteTurn) {
            addText("${nodeToAdd.relatedPosition.info.moveNumber}.")
        }

        if (nodeToAdd.parentNode == null) {
            addMoveLink("|", nodeToAdd)
        }
        else {
            addMoveLink(nodeToAdd.moveLeadingToThisNodeFAN!!, nodeToAdd)
        }
    }

    fun addFirstMainLineMove(nodeToAdd: HistoryNode) {
        if (nodeToAdd.variants.isNotEmpty()) {
            // If we have variants, the first main line move must be placed here
            if (nodeToAdd.mainLine!=null && !nodeToAdd.mainLine!!.relatedPosition.info.whiteTurn) {
                addText("${nodeToAdd.mainLine!!.relatedPosition.info.moveNumber}.")
            }
            addMoveLink(nodeToAdd.mainLine!!.moveLeadingToThisNodeFAN!!, nodeToAdd.mainLine!!)
        }
    }

    fun addVariantsMoves(nodeToAdd: HistoryNode) {
        val tabulation = "    "
        nodeToAdd.variants.forEach { currentChild ->
            tabLevel++
            addText("\n")
            (1..tabLevel).forEach { addText(tabulation) }
            addText("(\n")
            (1..tabLevel).forEach { addText(tabulation) }

            if (!currentChild.parentNode!!.relatedPosition.info.whiteTurn)
                addText("${currentChild.parentNode.relatedPosition.info.moveNumber}...")

            addAllMovesFromNode(currentChild)

            addText("\n")
            (1..tabLevel).forEach { addText(tabulation) }
            addText(")\n")
            tabLevel--
            (1..tabLevel).forEach { addText(tabulation) }
        }
    }

    fun addAllMovesFromNode(nodeToAdd: HistoryNode, includeHeadMove: Boolean = true) {
        if (includeHeadMove) addHeadMove(nodeToAdd)
        else if (nodeToAdd.mainLine?.relatedPosition?.info?.whiteTurn ?:false) {
            // We need to check for the presence of the mainline
            // and so, the move number is the one of the node to add, not the main line first move.
            addText("${nodeToAdd.relatedPosition.info.moveNumber}...")
        }
        addFirstMainLineMove(nodeToAdd)
        addVariantsMoves(nodeToAdd)
        if (nodeToAdd.mainLine != null) {
            // If we have variants, no need to duplicate the first main line move
            val needingHeadMove = nodeToAdd.variants.isEmpty()
            addAllMovesFromNode(nodeToAdd.mainLine!!, includeHeadMove = needingHeadMove)
        }
    }
}

class MoveLink(moveText: String, val relatedHistoryNode: HistoryNode, val parentView: MovesHistory) : Hyperlink(moveText){
    init {
        setOnAction {
            parentView.fire(ChangeChessBoardPosition(historyNode = relatedHistoryNode))
        }
    }
}