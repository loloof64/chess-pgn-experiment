package com.loloof64.chess_core.history

import com.loloof64.chess_core.game.ChessGame

/** Notice that when creating an HistoryNode, it will automatically be added to the given parent node */
class HistoryNode(val relatedPosition: ChessGame, val parentNode: HistoryNode?) {
    init {
        parentNode?.addChild(this)
    }

    private val children = mutableListOf<HistoryNode>()

    /** Notice that the method first checks if the child is not yet added */
    fun addChild(child: HistoryNode) {
        if (child !in children) children.add(child)
    }

    fun removeChild(child: HistoryNode) {
        children.remove(child)
    }
}
