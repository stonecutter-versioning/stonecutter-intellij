package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.nextLeafs
import com.intellij.psi.util.startOffset
import com.intellij.util.takeWhileInclusive
import dev.kikugie.stonecutter.intellij.lang.layout.buildStitcherAst
import dev.kikugie.stonecutter.intellij.lang.util.UserDataHolderAccessor
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import dev.kikugie.stonecutter.intellij.lang.util.stitcherCode

/**
 * Represents a block element in the Stitcher PSI structure.
 *
 * This structure is laid on top of the file's main PSI.
 */
sealed interface PsiBlock : PsiElement, UserDataHolderAccessor {
    fun <T> accept(visitor: Visitor<T>): T

    interface Visitor<T> {
        fun visitContent(content: Content): T
        fun visitComment(comment: Comment): T
        fun visitCode(code: Code): T
        fun visitRoot(root: Root): T
    }
    
    class Content(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock {
        val hostElements: Sequence<PsiElement> get() = buildElementSequence().orEmpty()

        var firstLeaf: SmartPsiElementPointer<PsiElement>? by CONTENT_FIRST_KEY
        var lastLeaf: SmartPsiElementPointer<PsiElement>? by CONTENT_LAST_KEY

        var localStart: Int? by CONTENT_START_KEY
        var localEnd: Int? by CONTENT_START_KEY

        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitContent(this)

        private fun buildElementSequence(): Sequence<PsiElement>? {
            val first = firstLeaf?.element ?: return null
            val last = lastLeaf?.element ?: return null
            return first.nextLeafs.takeWhileInclusive { it != last }
        }
    }
    
    class Comment(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock {
        var hostComment: SmartPsiElementPointer<PsiComment>? by COMMENT_HOST_KEY

        var localStart: Int? by CONTENT_START_KEY
        var localEnd: Int? by CONTENT_END_KEY

        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitComment(this)
    }
    
    class Code(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock {
        val entries: Sequence<PsiBlock> = childrenSequence.filterIsInstance<PsiBlock>()
        var hostComment: SmartPsiElementPointer<PsiComment>? by COMMENT_HOST_KEY

        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitCode(this)
    }
    
    class Root(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock {
        val entries: Sequence<PsiBlock> = childrenSequence.filterIsInstance<PsiBlock>()

        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitRoot(this)
    }

    companion object {
        val CONTENT_FIRST_KEY: Key<SmartPsiElementPointer<PsiElement>> = Key.create("STITCHER_CONTENT_FIRST")
        val CONTENT_LAST_KEY: Key<SmartPsiElementPointer<PsiElement>> = Key.create("STITCHER_CONTENT_LAST")

        val CONTENT_START_KEY: Key<Int> = Key.create("STITCHER_CONTENT_START")
        val CONTENT_END_KEY: Key<Int> = Key.create("STITCHER_CONTENT_END")

        val COMMENT_HOST_KEY: Key<SmartPsiElementPointer<PsiComment>> = Key.create("STITCHER_COMMENT_HOST")

        val ROOT_BLOCK_KEY: Key<CachedValue<Result<Root>>> = Key.create("STITCHER_ROOT_BLOCK")
    }
}

/**
 * Returns `true` if all children are either [PsiBlock.Comment] or blank [PsiBlock.Content].
 */
val PsiBlock.Code.isCommentedOut: Boolean
    get() = entries.all { it.accept(IsCommentedChecker) }

val PsiBlock.Code.definition: PsiDefinition?
    get() = hostComment?.element?.stitcherCode?.element?.definition

/**
 * Gets or constructs the cached [PsiBlock.Root] for this file.
 *
 * This function traverses the entire file AST on the first pass,
 * so use it sparingly.
 */
fun PsiFile.getStitcherAst(): PsiBlock.Root? {
    val file = InjectedLanguageManager.getInstance(project).getTopLevelFile(this) ?: this
    return CachedValuesManager.getCachedValue(file, PsiBlock.ROOT_BLOCK_KEY) {
        CachedValueProvider.Result.create(file.buildStitcherAst(), file, PsiModificationTracker.MODIFICATION_COUNT)
    }.getOrNull()
}

/**
 * Returns the deepmost [PsiBlock] this element belongs to,
 * constructing the AST if necessary.
 *
 * This function traverses the Stitcher block AST
 * and may traverse the entire file AST on the first pass,
 * so use it sparingly.
 */
fun PsiElement.findHostedBlock(): PsiBlock? =
    containingFile.getStitcherAst()?.accept(HostBlockLocator(this))

private class HostBlockLocator(val host: PsiElement) : PsiBlock.Visitor<PsiBlock?> {
    override fun visitContent(content: PsiBlock.Content): PsiBlock? = when {
        host.textRange !in content.textRange -> null
        content.hostElements.none { it == host } -> null
        else -> content
    }

    override fun visitComment(comment: PsiBlock.Comment): PsiBlock? =
        comment.takeIf { it.hostComment?.element == host }

    override fun visitCode(code: PsiBlock.Code): PsiBlock? = when {
        code.hostComment?.element == host -> code
        host.textRange !in TextRange(code.firstChild.startOffset, code.lastChild.endOffset) -> null
        else -> code.entries.firstNotNullOfOrNull { it.accept(this) }
    }

    override fun visitRoot(root: PsiBlock.Root): PsiBlock? =
        root.entries.firstNotNullOfOrNull { it.accept(this) }
}

private object IsCommentedChecker : PsiBlock.Visitor<Boolean> {
    override fun visitContent(content: PsiBlock.Content): Boolean =
        content.hostElements.all { it is PsiWhiteSpace }

    override fun visitComment(comment: PsiBlock.Comment): Boolean = true

    override fun visitCode(code: PsiBlock.Code): Boolean = false

    override fun visitRoot(root: PsiBlock.Root): Boolean =
        throw UnsupportedOperationException("This operation can't be called on the root block")
}
