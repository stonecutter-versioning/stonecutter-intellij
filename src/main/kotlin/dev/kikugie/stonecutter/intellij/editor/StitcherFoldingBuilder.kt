package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import dev.kikugie.commons.takeAsOrNull
import dev.kikugie.stonecutter.intellij.lang.psi.PsiBlock
import dev.kikugie.stonecutter.intellij.lang.psi.PsiCondition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.getStitcherAst
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import dev.kikugie.stonecutter.intellij.lang.util.stitcherCode
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings
import dev.kikugie.stonecutter.intellij.settings.variants.FoldingMode
import dev.kikugie.stonecutter.intellij.settings.variants.FoldingStyle
import kotlin.math.max
import kotlin.math.min

class StitcherFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun isCollapsedByDefault(node: ASTNode): Boolean = true
    override fun getPlaceholderText(node: ASTNode): String = ""
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<out FoldingDescriptor> {
        if (StonecutterSettings.STATE.foldDisabledBlocks == FoldingMode.DISABLED) return emptyArray()

        val ast = root.containingFile.getStitcherAst() ?: return emptyArray()
        val visitor = FoldingVisitor().apply { ast.accept(this) }
        return visitor.descriptors.toTypedArray()
    }

    object Constants {
        @JvmField val STITCHER_SCOPE = FoldingGroup.newGroup("stitcher-scope")
    }

    private class FoldingVisitor(val descriptors: MutableList<FoldingDescriptor> = mutableListOf()) : PsiBlock.Visitor<FoldingVisitor.Region> {
        data class Region(val start: Int, val end: Int, val psi: PsiBlock, val element: PsiComment? = null, val enabled: Boolean = true)

        override fun visitContent(content: PsiBlock.Content): Region {
            ProgressManager.checkCanceled()
            return Region(content.startOffset, content.endOffset, content, enabled = content.text.isNotBlank())
        }

        override fun visitComment(comment: PsiBlock.Comment): Region {
            ProgressManager.checkCanceled()
            return Region(comment.startOffset, comment.endOffset, comment, enabled = false)
        }

        override fun visitCode(code: PsiBlock.Code): Region {
            ProgressManager.checkCanceled()
            val comment = code.hostComment?.element
                ?: return Region(code.startOffset, code.endOffset, code)

            val condition = comment.takeIf { comment.definitionOrNull() is PsiCondition }
            val result = visitEntries(code)
            return result.copy(start = code.startOffset, element = condition, enabled = result.enabled || condition == null)
        }

        override fun visitRoot(root: PsiBlock.Root): Region =
            visitEntries(root)

        private fun visitEntries(block: PsiBlock): Region {
            val chain = mutableListOf<Region>()

            val entries = block.childrenSequence.mapNotNull { it.takeAsOrNull<PsiBlock>()?.accept(this) }.toList()
            if (entries.isEmpty()) return Region(block.startOffset, block.endOffset, block)

            var enabled = false
            var start = 0
            var end = 0
            for (region in entries) {
                start = min(start, region.start)
                end = max(end, region.end)
                enabled = enabled || region.enabled || region.element != null

                if (region.element != null && !region.enabled) chain += region else {
                    submitConditionChain(chain)
                    chain.clear()
                }
            }

            return Region(start, end, block, enabled = enabled)
        }

        private fun submitConditionChain(chain: List<Region>) {
            if (chain.isEmpty()) return
            val comments = chain.mapNotNull(Region::element).ifEmpty { return }
            val primary = comments.first()
            val group = Constants.STITCHER_SCOPE.takeIf { StonecutterSettings.STATE.linkDisabledBlocks }
            val title = StringBuilder("? ")

            comments.joinTo(title, " ... ") { it.definitionOrNull()?.text.orEmpty() }
            if (!chain.last().enabled) title.append(" ...")

            descriptors += FoldingDescriptor(
                primary,
                chain.first().start,
                chain.last().end,
                group,
                buildPlaceholderText(primary, title.toString())
            )
        }

        private fun buildPlaceholderText(primary: PsiComment, title: String): String = when (StonecutterSettings.STATE.foldedPresentation) {
            FoldingStyle.HIDE_ALL -> title
            FoldingStyle.KEEP_COMMENTS -> ElementManipulators.getValueTextRange(primary)
                .replace(primary.text, title)
            FoldingStyle.HIDE_INLINE -> {
                val range = ElementManipulators.getValueTextRange(primary)
                val line = primary.text.substring(range.endOffset).isBlank()
                if (line) range.replace(primary.text, title) else title
            }
        }

        private fun PsiComment.definitionOrNull(): PsiDefinition? =
            stitcherCode?.element?.definition
    }
}