package dev.kikugie.stonecutter.intellij.lang.layout

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import dev.kikugie.stonecutter.intellij.lang.psi.PsiBlock
import dev.kikugie.stonecutter.intellij.lang.psi.PsiDefinition.Kind
import dev.kikugie.stonecutter.intellij.lang.psi.PsiScope
import dev.kikugie.stonecutter.intellij.lang.util.canHasStitcherCode
import dev.kikugie.stonecutter.intellij.lang.util.stitcherCode
import dev.kikugie.stonecutter.intellij.lang.util.unquote
import java.util.*

private val LINE_BREAKS: CharArray = charArrayOf('\r', '\n')
private val WORD_BREAKS: CharArray = charArrayOf(' ', '\t')
private val WHITESPACES: CharArray = WORD_BREAKS + LINE_BREAKS

fun PsiFile.buildStitcherAst(): PsiBlock.Root {
    val visitor = LayoutBuildingVisitor()
    accept(visitor)
    return visitor.root
}

private class LayoutBuildingVisitor : PsiRecursiveElementVisitor() {
    private val stack: Deque<PsiBlockBuilder.Scoped> = ArrayDeque()
    private val elements: MutableList<PsiElement> = mutableListOf()
    private var built: PsiBlock.Root? = null

    val root: PsiBlock.Root
        get() = checkNotNull(built) { "Root scope is not built yet" }

    override fun visitFile(psiFile: PsiFile) {
        stack.clear()
        stack.addLast(RootBuilder())
        elements.clear()
        built = null

        super.visitFile(psiFile)
        handleContent()

        while (stack.isNotEmpty()) when (val it = stack.peekLast()) {
            is RootBuilder -> built = it.build()
            is CodeBuilder -> it.finalize(true)
        }
    }

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        if (element.firstChild == null) elements += element
        else element.acceptChildren(this)
    }

    override fun visitComment(comment: PsiComment) {
        ProgressIndicatorProvider.checkCanceled()
        handleContent()
        handleComment(comment)
    }

    private tailrec fun acceptBlock(block: PsiBlockBuilder): Unit = when(val result = stack.peekLast().accept(block)) {
        BlockAcceptResult.ConsumedOpen -> Unit
        BlockAcceptResult.ConsumedFinal -> stack.removeLast().finalize(false)
        BlockAcceptResult.Rejected -> {
            stack.removeLast().finalize(false)
            acceptBlock(block)
        }
        is BlockAcceptResult.ConsumedPartial -> {
            stack.removeLast().finalize(false)
            acceptBlock(result.remainder)
        }
    }

    private fun handleContent() {
        if (elements.isNotEmpty()) acceptBlock(ContentBuilder(elements))
        this.elements.clear()
    }

    private fun handleComment(comment: PsiComment) {
        if (!comment.canHasStitcherCode)
            return acceptBlock(CommentBuilder(comment))

        val code = CodeBuilder(comment)
        if (code.kind == Kind.INDEPENDENT) acceptBlock(code)
        else handleCode(code)
    }

    private fun handleCode(code: CodeBuilder) = when(stack.peekLast()) {
        is RootBuilder -> handleRootCode(code)
        is CodeBuilder -> handleNestedCode(code)
    }

    private fun handleRootCode(code: CodeBuilder) {
        acceptBlock(code)
        if (code.kind != Kind.CLOSER) stack.addLast(code)
    }

    private fun handleNestedCode(code: CodeBuilder) {
        if (code.kind.isExtension)
            stack.removeLast().finalize(false)

        acceptBlock(code)
        if (!code.kind.isEmpty) stack.addLast(code)
    }
}

private sealed interface BlockAcceptResult {
    object Rejected : BlockAcceptResult
    object ConsumedOpen : BlockAcceptResult
    object ConsumedFinal : BlockAcceptResult
    class ConsumedPartial(val remainder: PsiBlockBuilder) : BlockAcceptResult
}

private sealed interface PsiBlockBuilder {
    fun build(): PsiBlock

    sealed interface Scoped : PsiBlockBuilder {
        val entries: List<PsiBlockBuilder>

        fun finalize(eof: Boolean) {}
        fun accept(block: PsiBlockBuilder): BlockAcceptResult
    }
}

private data class ContentBuilder(
    val elements: List<PsiElement>,
    val start: Int = 0,
    val end: Int = elements.last().endOffset - elements.first().startOffset
) : PsiBlockBuilder {
    val text by lazy { elements.joinToString("", transform = PsiElement::getText) }
    val length: Int inline get() = end - start

    override fun build(): PsiBlock.Content = PsiBlock.Content(LeafPsiElement(StitcherBlockType.CONTENT.asIElementType(), text)).apply {
        firstLeaf = SmartPointerManager.createPointer(elements.first())
        lastLeaf = SmartPointerManager.createPointer(elements.last())

        if (start > 0) localStart = start
        if (end < this@ContentBuilder.text.length)
            localEnd = end
    }
}

private data class CommentBuilder(
    val comment: PsiComment,
    val start: Int = 0,
    val end: Int = comment.textLength
) : PsiBlockBuilder {
    val text: String by lazy { ElementManipulators.getValueText(comment) }
    val length: Int inline get() = end - start

    override fun build(): PsiBlock.Comment = PsiBlock.Comment(LeafPsiElement(StitcherBlockType.COMMENT.asIElementType(), comment.text)).apply {
        hostComment = SmartPointerManager.createPointer(comment)

        if (start > 0) localStart = start
        if (end < comment.textLength)
            localEnd = end
    }
}

private class RootBuilder(override val entries: MutableList<PsiBlockBuilder> = mutableListOf()) : PsiBlockBuilder.Scoped {
    override fun accept(block: PsiBlockBuilder): BlockAcceptResult = BlockAcceptResult.ConsumedOpen.also { entries += block }
    override fun build(): PsiBlock.Root = PsiBlock.Root(CompositeElement(StitcherBlockType.ROOT.asIElementType())).apply {
        for (it in entries) add(it.build())
    }
}

private class CodeBuilder(val host: PsiComment, override val entries: MutableList<PsiBlockBuilder> = mutableListOf()) : PsiBlockBuilder.Scoped {
    val code = checkNotNull(host.stitcherCode?.element) { "${host.text} doesn't have Stitcher code" }
    val kind: Kind inline get() = code.definition?.kind ?: Kind.INDEPENDENT
    var satisfied: Boolean = kind.isScoped
        private set

    override fun accept(block: PsiBlockBuilder): BlockAcceptResult = when(kind) {
        Kind.SCOPED_OPENER, Kind.SCOPED_EXTENSION -> BlockAcceptResult.ConsumedOpen.also { entries += block }
        Kind.LINE_OPENER, Kind.LINE_EXTENSION -> if (satisfied) BlockAcceptResult.Rejected else acceptLine(block)
        Kind.LOOKUP_OPENER, Kind.LOOKUP_EXTENSION -> if (satisfied) BlockAcceptResult.Rejected else acceptLookup(block)
        else -> BlockAcceptResult.Rejected
    }

    override fun build(): PsiBlock.Code = PsiBlock.Code(CompositeElement(StitcherBlockType.CODE.asIElementType())).apply {
        for (it in entries) add(it.build())

        hostComment = SmartPointerManager.createPointer(host)
    }

    private fun acceptLine(block: PsiBlockBuilder): BlockAcceptResult = when (block) {
        is ContentBuilder -> {
            val split = findLineSplit(block.text)
            consumeContentSplit(block, split)
        }
        is CommentBuilder -> {
            val split = findLineSplit(block.text)
            consumeCommentSplit(block, split)
        }
        else -> consumeFinal(block)
    }

    private fun acceptLookup(block: PsiBlockBuilder): BlockAcceptResult {
        if (block is PsiBlockBuilder.Scoped) return consumeFinal(block)

        val opener = code.definition?.opener as? PsiScope.Lookup
            ?: return BlockAcceptResult.Rejected
        return acceptLookup(block, opener)
    }

    private fun acceptLookup(block: PsiBlockBuilder, scope: PsiScope.Lookup): BlockAcceptResult = when (block) {
        is ContentBuilder -> {
            val lookup = scope.lookup
            val split = if (lookup == null) findDefaultLookupSplit(block.text)
            else findCustomLookupSplit(block.text, lookup.unquote(), scope.plus != null)
            consumeContentSplit(block, split)
        }
        is CommentBuilder -> {
            val lookup = scope.lookup
            val split = if (lookup == null) findDefaultLookupSplit(block.text)
            else findCustomLookupSplit(block.text, lookup.unquote(), scope.plus != null)
            consumeCommentSplit(block, split)
        }
        else -> BlockAcceptResult.Rejected
    }

    private fun consumeFinal(block: PsiBlockBuilder): BlockAcceptResult {
        satisfied = true
        entries += block
        return BlockAcceptResult.ConsumedFinal
    }

    private fun consumeContentSplit(block: ContentBuilder, split: Int) : BlockAcceptResult = when(split) {
        -1 -> BlockAcceptResult.ConsumedOpen.also { entries += block }
        0 -> BlockAcceptResult.Rejected.also { satisfied = true }
        block.length -> consumeFinal(block)
        else -> {
            satisfied = true
            val first = block.elements.takeWhile { it.endOffset <= split }
            val second = block.elements.dropWhile { it.startOffset < split }
            entries += ContentBuilder(first, end = split)
            BlockAcceptResult.ConsumedPartial(ContentBuilder(second, start = split))
        }
    }

    private fun consumeCommentSplit(block: CommentBuilder, split: Int) : BlockAcceptResult = when(split) {
        -1 -> BlockAcceptResult.ConsumedOpen.also { entries += block }
        0 -> BlockAcceptResult.Rejected.also { satisfied = true }
        block.length -> consumeFinal(block)
        else -> {
            satisfied = true
            val range = ElementManipulators.getValueTextRange(block.comment)
            entries += block.copy(end = split + range.startOffset)
            BlockAcceptResult.ConsumedPartial(block.copy(start = split + range.startOffset))
        }
    }
}

private fun findLineSplit(str: String): Int {
    var start = 0
    while (true) when (val index = str.indexOfAny(LINE_BREAKS, start)) {
        -1 -> return if (start == 0) -1 else start + 1
        else -> {
            start = index + 1
            for (i in start..<index) if (str[i] !in WORD_BREAKS)
                return start
        }
    }
}

private fun findDefaultLookupSplit(str: String): Int {
    val offset = str.indexOfFirst { it !in WHITESPACES }
    if (offset == -1) return -1

    val result = str.indexOfAny(WHITESPACES, offset)
    return if (result == -1) str.length else result
}

private fun findCustomLookupSplit(str: String, pattern: String, capturing: Boolean): Int {
    val result = str.indexOf(pattern)
    return if (result == -1) str.length
    else if (capturing) result + pattern.length
    else result
}
