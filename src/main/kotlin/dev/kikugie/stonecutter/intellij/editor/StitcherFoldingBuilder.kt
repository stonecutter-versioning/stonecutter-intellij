package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.descendantsOfType
import dev.kikugie.commons.text.countMatching
import dev.kikugie.commons.text.reverseView
import dev.kikugie.stonecutter.intellij.lang.access.ScopeDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherCondition
import dev.kikugie.stonecutter.intellij.util.stitcherFile

class StitcherFoldingBuilder : FoldingBuilderEx(), DumbAware {
    object Constants {
        @JvmField val STITCHER_SCOPE = FoldingGroup.newGroup("stitcher-scope")
    }

    private enum class CommentType { INDEPENDENT, SCOPED, CLOSED, OPEN }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = true
    override fun getPlaceholderText(node: ASTNode): String = "<disabled>"
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<out FoldingDescriptor> {
        val comments: MutableList<Pair<PsiComment, CommentType>> = mutableListOf()
        PsiTreeUtil.findChildrenOfType(root, PsiComment::class.java).mapTo(comments) { it to it.type(comments.lastOrNull()) }
        return comments.mapNotNull { (comment, type) -> fold(comment, type) }.toTypedArray()
    }

    private fun fold(comment: PsiComment, type: CommentType): FoldingDescriptor? {
        if (type != CommentType.SCOPED) return null
        return FoldingDescriptor(comment.node, comment.determineFoldingRange(), Constants.STITCHER_SCOPE)
    }

    private fun PsiComment.type(previous: Pair<PsiComment, CommentType>?): CommentType = when (val injected = stitcherFile) {
        null -> previous?.let { (comment, type) ->
            val last = lastSiblings().find { it !is PsiWhiteSpace }
            if (last == null || last !== comment) CommentType.INDEPENDENT
            else if (type == CommentType.INDEPENDENT || type == CommentType.CLOSED) CommentType.INDEPENDENT
            else CommentType.SCOPED
        } ?: CommentType.INDEPENDENT

        else -> when (val def = injected.descendantsOfType<ScopeDefinition>().first()) {
            is StitcherCondition -> if (def.opener != null || def.expression != null || def.sugar.firstOrNull() != null)
                CommentType.OPEN else CommentType.CLOSED

            else -> CommentType.CLOSED
        }
    }

    private fun PsiComment.determineFoldingRange(): TextRange {
        val local = ElementManipulators.getValueTextRange(this)
        var count = local.substring(text).reverseView().countMatching(' ', '\t', '\r', '\n')
        if (count == 0) return textRange

        count += textLength - local.endOffset
        return TextRange(textRange.startOffset, textRange.endOffset - count)
    }

    private fun PsiComment.lastSiblings(): Sequence<PsiElement> = generateSequence({ prevSibling }, { it.prevSibling })
}