package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.kikugie.stonecutter.intellij.lang.access.OpenerType
import dev.kikugie.stonecutter.intellij.lang.access.ScopeType
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherCondition
import dev.kikugie.stonecutter.intellij.lang.util.commentDefinition
import dev.kikugie.stonecutter.intellij.lang.util.openerType
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings
import dev.kikugie.stonecutter.intellij.settings.variants.FoldingMode
import dev.kikugie.stonecutter.intellij.settings.variants.FoldingStyle
import dev.kikugie.stonecutter.intellij.util.filterNotWhitespace
import dev.kikugie.stonecutter.intellij.util.prevSiblings

class StitcherFoldingBuilder : FoldingBuilderEx(), DumbAware {
    object Constants {
        @JvmField val STITCHER_SCOPE = FoldingGroup.newGroup("stitcher-scope")
        private val GROUPED: MutableList<FoldingGroup> = mutableListOf()

        fun group(n: Int): FoldingGroup = when(n) {
            in GROUPED.indices -> GROUPED[n]
            GROUPED.size -> FoldingGroup.newGroup("stitcher-scope-$n").also(GROUPED::add)
            else -> error("Attempted to skip groups ${GROUPED.size} -> $n")
        }
    }

    private enum class CommentType { INDEPENDENT, SCOPED, OPEN, EXTENSION, CLOSED }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = true
    override fun getPlaceholderText(node: ASTNode): String = ""
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<out FoldingDescriptor> {
        if (StonecutterSettings.STATE.foldDisabledBlocks == FoldingMode.DISABLED) return emptyArray()

        val comments: MutableList<Pair<PsiComment, CommentType>> = mutableListOf()
        PsiTreeUtil.findChildrenOfType(root, PsiComment::class.java).mapTo(comments) { it to it.type(comments.lastOrNull()) }
        return buildFoldingDescriptors(comments).toTypedArray()
    }

    private fun PsiComment.type(previous: Pair<PsiComment, CommentType>?): CommentType = when (val injected = commentDefinition) {
        null -> previous?.let { (comment, type) ->
            val last = prevSiblings.filterNotWhitespace().firstOrNull()
            if (last == null || last !== comment) CommentType.INDEPENDENT
            else if (type == CommentType.INDEPENDENT || type == CommentType.CLOSED) CommentType.INDEPENDENT
            else CommentType.SCOPED
        } ?: CommentType.INDEPENDENT

        else -> when (val def = injected.element) {
            is StitcherCondition -> when (def.type) {
                ScopeType.OPENER -> CommentType.OPEN
                ScopeType.EXTENSION -> CommentType.EXTENSION
                ScopeType.CLOSER -> CommentType.CLOSED
                ScopeType.INVALID -> CommentType.INDEPENDENT
            }

            else -> CommentType.INDEPENDENT
        }
    }

    private fun TextRange.expand(element: PsiElement) =
        if (this === TextRange.EMPTY_RANGE) element.textRange
        else union(element.textRange)

    private fun buildFoldingDescriptors(list: List<Pair<PsiComment, CommentType>>): List<FoldingDescriptor> = buildList {
        val conditions: MutableList<PsiComment> = mutableListOf()
        var range: TextRange = TextRange.EMPTY_RANGE
        var index = 0

        fun submit(increment: Boolean) {
            if (conditions.isEmpty()) return
            if (conditions.first().textRange != range) this += buildFoldingDescriptor(conditions, range, index)
                .also { if (increment) index++ }
            conditions.clear(); range = TextRange.EMPTY_RANGE
        }

        for ((index, pair) in list.withIndex()) when (pair.second) {
            CommentType.OPEN -> {
                submit(false)
                conditions += pair.first
                range = range.expand(pair.first)
            }

            CommentType.EXTENSION -> {
                if (list.getOrNull(index - 1)?.second != CommentType.SCOPED)
                    submit(true)
                conditions += pair.first
                range = range.expand(pair.first)
            }

            CommentType.CLOSED -> if (list.getOrNull(index - 1)?.second == CommentType.SCOPED) {
                if (conditions.lastOrNull()?.commentDefinition?.element?.opener?.openerType == OpenerType.OPEN) {
                    conditions += pair.first
                    range = range.expand(pair.first)
                }
                submit(true)
            }

            CommentType.SCOPED -> {
                range = range.expand(pair.first)
            }

            CommentType.INDEPENDENT -> submit(true)
        }

        if (conditions.isNotEmpty()) submit(true)
    }

    private fun buildFoldingDescriptor(conditions: List<PsiComment>, range: TextRange, index: Int): FoldingDescriptor {
        val primary = conditions.first()
        val last = conditions.last()
        val title = StringBuilder()
        conditions.joinTo(title, " ... ") { it.commentDefinition!!.element!!.text.trim(' ', '\t', '?') }
        if (title.startsWith('}')) title.insert(0, "?") else title.insert(0, "? ")
        if (last.textRange.endOffset != range.endOffset) title.append(" ...")

        val group = if (StonecutterSettings.STATE.linkDisabledBlocks) Constants.STITCHER_SCOPE
        else Constants.group(index)
        return FoldingDescriptor(primary.node, range, group).apply {
            placeholderText = buildPlaceholderText(primary, title.toString())
        }
    }

    private fun buildPlaceholderText(primary: PsiComment, title: String): String {
        val mode = StonecutterSettings.STATE.foldedPresentation
        if (mode == FoldingStyle.HIDE_ALL) return title
        val content = primary.text
        val valueRange = ElementManipulators.getValueTextRange(primary)

        if (mode == FoldingStyle.KEEP_COMMENTS) return valueRange.replace(content, title)
        val isLineComment = content.substring(valueRange.endOffset).isBlank()
        return if (isLineComment) valueRange.replace(content, title) else title
    }
}