package com.loloof64.chess_pgn_experiment

import com.loloof64.chess_core.pieces.*
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.Callback
import tornadofx.*

class PromotionDialog(whiteTurn: Boolean) : Dialog<PromotablePiece>() {

    companion object {
        private val picturesSize = 75.0
        private val wantedSize = 60.0
        private val picturesScales = wantedSize / picturesSize
    }

    private var selectedPieceValue: PromotablePiece = Queen(whiteTurn)

    init {
        title = "Choosing the promotion piece"
        headerText = "Choose your promotion piece"
        graphic = ImageView(if (whiteTurn) "chess_pl.png" else "chess_pd.png")

        dialogPane.buttonTypes.add(ButtonType.OK)

        val queenImageView = ImageView(if (whiteTurn) "chess_ql.png" else "chess_qd.png")
        val rookImageView = ImageView(if (whiteTurn) "chess_rl.png" else "chess_rd.png")
        val bishopImageView = ImageView(if (whiteTurn) "chess_bl.png" else "chess_bd.png")
        val knightImageView = ImageView(if (whiteTurn) "chess_nl.png" else "chess_nd.png")

        val selectionButtonsLine = HBox()
        val selectedPiecePreviewLine = HBox(Label("Selected piece"))

        arrayOf(queenImageView, rookImageView, bishopImageView, knightImageView).forEach {
            it.scaleX = picturesScales
            it.scaleY = picturesScales
        }

        var selectedPieceImageView = ImageView(if (whiteTurn) "chess_ql.png" else "chess_qd.png").apply {
            scaleX = picturesScales
            scaleY = picturesScales
        }

        val queenButton = Button("", queenImageView).apply {
            setOnAction {
                selectedPieceValue = Queen(whiteTurn)
                selectedPieceImageView = ImageView(if (whiteTurn) "chess_ql.png" else "chess_qd.png").apply {
                    scaleX = picturesScales
                    scaleY = picturesScales
                }
                selectedPiecePreviewLine.children[1] = selectedPieceImageView
            }
        }

        val rookButton = Button("", rookImageView).apply {
            setOnAction {
                selectedPieceValue = Rook(whiteTurn)
                selectedPieceImageView = ImageView(if (whiteTurn) "chess_rl.png" else "chess_rd.png").apply {
                    scaleX = picturesScales
                    scaleY = picturesScales
                }
                selectedPiecePreviewLine.children[1] = selectedPieceImageView
            }
        }

        val bishopButton = Button("", bishopImageView).apply {
            setOnAction {
                selectedPieceValue = Bishop(whiteTurn)
                selectedPieceImageView = ImageView(if (whiteTurn) "chess_bl.png" else "chess_bd.png").apply {
                    scaleX = picturesScales
                    scaleY = picturesScales
                }
                selectedPiecePreviewLine.children[1] = selectedPieceImageView
            }
        }

        val knightButton = Button("", knightImageView).apply {
            setOnAction {
                selectedPieceValue = Knight(whiteTurn)
                selectedPieceImageView = ImageView(if (whiteTurn) "chess_nl.png" else "chess_nd.png").apply {
                    scaleX = picturesScales
                    scaleY = picturesScales
                }
                selectedPiecePreviewLine.children[1] = selectedPieceImageView
            }
        }

        selectionButtonsLine.add(queenButton)
        selectionButtonsLine.add(rookButton)
        selectionButtonsLine.add(bishopButton)
        selectionButtonsLine.add(knightButton)

        selectedPiecePreviewLine.add(selectedPieceImageView)

        val customView = VBox(selectionButtonsLine, selectedPiecePreviewLine)

        dialogPane.content = customView

        resultConverter = Callback{ selectedPieceValue }
    }
}
