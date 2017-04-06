package com.loloof64.chess_core.history

import com.loloof64.chess_core.game.ChessGame

/** Notice that when creating an HistoryNode, it will automatically be added to the given parent node */
class HistoryNode(val relatedPosition: ChessGame, val parentNode: HistoryNode?,
                  val moveLeadingToThisNodeFAN: String?) {
    init {
        parentNode?.addChild(this)
        if (parentNode != null) require(moveLeadingToThisNodeFAN != null)
            {"Only the root node can bypass the moveLeadingToThisNodeFAN parameter."}
    }

    /**
     * First child belongs to the main line
     */
    private val _children = mutableListOf<HistoryNode>()

    /** Notice that the method first checks if the child is not yet added */
    fun addChild(child: HistoryNode) {
        if (child !in _children) _children.add(child)
    }

    fun removeChild(child: HistoryNode) {
        _children.remove(child)
    }

    val children : List<HistoryNode>
            get() = _children
}
