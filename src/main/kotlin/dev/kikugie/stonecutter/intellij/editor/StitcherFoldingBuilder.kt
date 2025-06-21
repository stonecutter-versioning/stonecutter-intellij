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
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import dev.kikugie.stonecutter.intellij.lang.access.ScopeDefinition.DefinitionType
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherCondition
import dev.kikugie.stonecutter.intellij.lang.util.commentDefinition
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings

class StitcherFoldingBuilder : FoldingBuilderEx(), DumbAware {
    object Constants {
        @JvmField val STITCHER_SCOPE = FoldingGroup.newGroup("stitcher-scope")
    }

    private enum class CommentType { INDEPENDENT, SCOPED, OPEN, EXTENSION, CLOSED }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = true
    override fun getPlaceholderText(node: ASTNode): String = ""
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<out FoldingDescriptor> {
        if (!StonecutterSettings.foldDisabledScopes) return emptyArray()

        val comments: MutableList<Pair<PsiComment, CommentType>> = mutableListOf()
        PsiTreeUtil.findChildrenOfType(root, PsiComment::class.java).mapTo(comments) { it to it.type(comments.lastOrNull()) }
        return buildFoldingDescriptors(comments).toTypedArray()
    }

    private fun PsiComment.lastSiblings(): Sequence<PsiElement> = generateSequence(prevSibling) { it.prevSibling }
    private fun PsiComment.type(previous: Pair<PsiComment, CommentType>?): CommentType = when (val injected = commentDefinition) {
        null -> previous?.let { (comment, type) ->
            val last = lastSiblings().find { it !is PsiWhiteSpace }
            if (last == null || last !== comment) CommentType.INDEPENDENT
            else if (type == CommentType.INDEPENDENT || type == CommentType.CLOSED) CommentType.INDEPENDENT
            else CommentType.SCOPED
        } ?: CommentType.INDEPENDENT

        else -> when (val def = injected.element) {
            is StitcherCondition -> when (def.type) {
                DefinitionType.OPENER -> CommentType.OPEN
                DefinitionType.EXTENSION -> CommentType.EXTENSION
                DefinitionType.CLOSER -> CommentType.CLOSED
                DefinitionType.INVALID -> CommentType.INDEPENDENT
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

        fun submit() {
            if (conditions.isEmpty()) return
            if (conditions.first().textRange != range) this += buildFoldingDescriptor(conditions, range)
            conditions.clear(); range = TextRange.EMPTY_RANGE
        }

        for ((index, pair) in list.withIndex()) when (pair.second) {
            CommentType.OPEN -> {
                submit()
                conditions += pair.first
                range = range.expand(pair.first)
            }

            CommentType.EXTENSION -> {
                if (list.getOrNull(index - 1)?.second != CommentType.SCOPED)
                    submit()
                conditions += pair.first
                range = range.expand(pair.first)
            }

            CommentType.CLOSED -> if (list.getOrNull(index - 1)?.second == CommentType.SCOPED) {
                if (conditions.lastOrNull()?.commentDefinition?.element?.closer?.text == "{") {
                    conditions += pair.first
                    range = range.expand(pair.first)
                }
                submit()
            }

            CommentType.SCOPED -> {
                range = range.expand(pair.first)
            }

            CommentType.INDEPENDENT -> submit()
        }

        if (conditions.isNotEmpty()) submit()
    }

    private fun buildFoldingDescriptor(conditions: List<PsiComment>, range: TextRange): FoldingDescriptor {
        val primary = conditions.first()
        val last = conditions.last()
        val title = StringBuilder()
        conditions.joinTo(title, " ... ") { it.commentDefinition!!.element!!.text.trim(' ', '\t', '?') }
        if (title.startsWith('}')) title.insert(0, "?") else title.insert(0, "? ")
        if (last.textRange.endOffset != range.endOffset) title.append(" ...")
        return FoldingDescriptor(primary.node, range, Constants.STITCHER_SCOPE).apply {
            placeholderText = ElementManipulators.getValueTextRange(primary).replace(primary.text, title.toString())
        }
    }
}