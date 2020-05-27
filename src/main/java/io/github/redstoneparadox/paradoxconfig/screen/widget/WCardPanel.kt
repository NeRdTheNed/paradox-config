package io.github.redstoneparadox.paradoxconfig.screen.widget

import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.WWidget
import net.fabricmc.api.EnvType
import kotlin.math.max
import net.fabricmc.api.Environment

// Thx Juuxel
class WCardPanel : WPanel(), PageContainer {
    val cardCount: Int get() = children.size
    var selectedCard: Int = 0
        private set

    override var currentPage: Int
        get() = selectedCard
        set(value) { selectedCard = value }
    override val pageCount get() = cardCount

    // Cards

    fun addCard(w: WWidget) {
        children += w

        w.setParent(this)
        w.setLocation(0, 0)
        expandToFit(w)
    }

    fun addCard(index: Int, w: WWidget) {
        children.add(index, w)

        w.setParent(this)
        w.setLocation(0, 0)
        expandToFit(w)
    }

    fun setCard(index: Int, w: WWidget) {
        children.set(index, w)

        w.setParent(this)
        w.setLocation(0,0)
        expandToFit(w)
    }

    fun select(card: Int) {
        require(card >= 0 && card < children.size) { "card must be a valid child index" }
        selectedCard = card
    }

    fun select(card: WWidget) =
        select(children.indexOf(card))

    fun selectPrevious() {
        if (selectedCard > 0) {
            selectedCard--
        }
    }

    fun selectNext() {
        if (selectedCard < children.lastIndex) {
            selectedCard++
        }
    }

    fun hasPrevious(): Boolean = selectedCard > 0
    fun hasNext(): Boolean = selectedCard < children.lastIndex

    // Mouse

    override fun onMouseUp(x: Int, y: Int, button: Int): WWidget {
        val child = children[selectedCard]
        return child.onMouseUp(x - child.x, y - child.y, button)
    }

    override fun onMouseDown(x: Int, y: Int, button: Int): WWidget {
        val child = children[selectedCard]
        return child.onMouseDown(x - child.x, y - child.y, button)
    }

    override fun onMouseDrag(x: Int, y: Int, button: Int) {
        val child = children[selectedCard]
        child.onMouseDrag(x - child.x, y - child.y, button)
    }

    override fun onClick(x: Int, y: Int, button: Int) {
        val child = children[selectedCard]
        child.onClick(x - child.x, y - child.y, button)
    }

    override fun hit(x: Int, y: Int): WWidget {
        val child = children[selectedCard]
        return child.hit(x - child.x, y - child.y)
    }

    // Layout

    override fun expandToFit(w: WWidget) {
        setSize(max(width, w.width), max(height, w.height))
    }

    override fun layout() {
        super.layout()
        children.forEach { it.setSize(width, height) }
    }

    // Painting

    @Environment(EnvType.CLIENT)
    override fun paintBackground(x: Int, y: Int, mouseX: Int, mouseY: Int) {
        backgroundPainter?.paintBackground(x, y, this)

        if (selectedCard < children.size) {
            val child = children[selectedCard]
            child.paintBackground(x + child.x, y + child.y, mouseX, mouseY)
        }
    }

    @Suppress("DEPRECATION")
    @Environment(EnvType.CLIENT)
    override fun paintForeground(x: Int, y: Int, mouseX: Int, mouseY: Int) {
        if (selectedCard < children.size) {
            val child = children[selectedCard]
            child.paintForeground(x + child.x, y + child.y, mouseX, mouseY)
        }
    }

    // Pages

    override fun hasPreviousPage() = hasPrevious()
    override fun hasNextPage() = hasNext()
    override fun showPreviousPage() = selectPrevious()
    override fun showNextPage() = selectNext()
}