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

    private var _mainLineChild : HistoryNode? = null

    private val _variantsChildren = mutableListOf<HistoryNode>()

    /** Notice that the method first checks if the child is not yet added */
    fun addChild(child: HistoryNode) {
        if (_mainLineChild == null) _mainLineChild = child
        if (child !in _variantsChildren && child != mainLine) _variantsChildren.add(child)
    }

    fun removeChild(child: HistoryNode) {
        _variantsChildren.remove(child)
    }

    fun promoteLine(lineIndex: Int) {
        if (lineIndex < 0 || lineIndex >= _variantsChildren.size) return
        if (_mainLineChild == null) throw NoMainVariationException()
        val temp = _variantsChildren[lineIndex]
        _variantsChildren[lineIndex] = _mainLineChild!!
        _mainLineChild = temp
    }

    val mainLine: HistoryNode?
        get() = _mainLineChild

    val variants: List<HistoryNode>
            get() = _variantsChildren
}

class NoMainVariationException : Exception()