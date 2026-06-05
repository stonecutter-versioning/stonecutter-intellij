package dev.kikugie.stonecutter.intellij.lang.layout

import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import dev.kikugie.commons.collections.lastNotNull
import dev.kikugie.commons.text.countMatching
import dev.kikugie.commons.text.countWhile
import dev.kikugie.commons.text.getOrDefault
import dev.kikugie.stonecutter.intellij.lang.psi.PsiBlock
import dev.kikugie.stonecutter.intellij.lang.psi.PsiDefinition.Kind
import dev.kikugie.stonecutter.intellij.lang.psi.PsiScope
import dev.kikugie.stonecutter.intellij.lang.util.canHasStitcherCode
import dev.kikugie.stonecutter.intellij.lang.util.stitcherCode
import dev.kikugie.stonecutter.intellij.lang.util.unquote
import java.util.*
import kotlin.math.max
import kotlin.math.min

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
    private var checkpoint: Int = 0
    private lateinit var contents: CharSequence
    private var built: PsiBlock.Root? = null

    val root: PsiBlock.Root
        get() = checkNotNull(built) { "Root scope is not built yet" }

    override fun visitFile(psiFile: PsiFile) {
        stack.clear()
        stack.addLast(RootBuilder())
        built = null
        checkpoint = psiFile.startOffset
        contents = psiFile.viewProvider.contents
        super.visitFile(psiFile)

        handleContent(contents.length, -1)
        while (stack.isNotEmpty()) when (val it = stack.peekLast()) {
            is RootBuilder -> built = it.build()
            is CodeBuilder -> it.finalize(true)
        }
    }

    override fun visitComment(comment: PsiComment) {
        handleContent(comment.startOffset, comment.endOffset)
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

    private fun handleContent(end: Int, next: Int) {
        if (checkpoint in 0..<end) acceptBlock(ContentBuilder(
            checkpoint, end, contents.substring(checkpoint, end)
        ))
        checkpoint = next
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

private data class ContentBuilder(val start: Int, val end: Int, val text: String) : PsiBlockBuilder {
    val length: Int inline get() = end - start

    override fun build(): PsiBlock = PsiBlock.Content(start, end)
}

private data class CommentBuilder(val comment: PsiComment, val start: Int = 0, val end: Int = comment.textLength) : PsiBlockBuilder {
    val text: String = ElementManipulators.getValueText(comment)
    val length: Int inline get() = end - start

    override fun build(): PsiBlock = PsiBlock.Comment(comment, start, end)
}

private class RootBuilder(override val entries: MutableList<PsiBlockBuilder> = mutableListOf()) : PsiBlockBuilder.Scoped {
    override fun accept(block: PsiBlockBuilder): BlockAcceptResult = entries.merge(block)
    override fun build(): PsiBlock.Root = PsiBlock.Root(entries.map(PsiBlockBuilder::build))
}

private class CodeBuilder(val host: PsiComment, override val entries: MutableList<PsiBlockBuilder> = mutableListOf()) : PsiBlockBuilder.Scoped {
    val code = checkNotNull(host.stitcherCode?.element) { "${host.text} doesn't have Stitcher code" }
    val kind: Kind inline get() = code.definition?.kind ?: Kind.INDEPENDENT
    var satisfied: Boolean = kind.isScoped
        private set

    override fun accept(block: PsiBlockBuilder): BlockAcceptResult = when(kind) {
        Kind.SCOPED_OPENER, Kind.SCOPED_EXTENSION -> entries.merge(block)
        Kind.LINE_OPENER, Kind.LINE_EXTENSION -> if (satisfied) BlockAcceptResult.Rejected else acceptLine(block)
        Kind.LOOKUP_OPENER, Kind.LOOKUP_EXTENSION -> if (satisfied) BlockAcceptResult.Rejected else acceptLookup(block)
        else -> BlockAcceptResult.Rejected
    }

    override fun build(): PsiBlock = PsiBlock.Code(host, entries.map(PsiBlockBuilder::build))

    private fun acceptLine(block: PsiBlockBuilder): BlockAcceptResult = when (block) {
        is ContentBuilder -> consumeContentSplit(block, consumeLine(block.text, entries.lastNotNull().isNotBlank()))
        is CommentBuilder -> consumeCommentSplit(block, consumeLine(block.text, entries.lastOrNull().isNotBlank())).let {
            if (it != BlockAcceptResult.ConsumedOpen) it
            else if (!block.isNotBlank() || block.text.getOrDefault(block.end - 1) !in LINE_BREAKS) it
            else BlockAcceptResult.ConsumedFinal
        }
        else -> consumeFinal(block)
    }

    private fun acceptLookup(block: PsiBlockBuilder): BlockAcceptResult {
        if (block is PsiBlockBuilder.Scoped) return consumeFinal(block)

        val opener = code.definition?.opener as? PsiScope.Lookup ?: return BlockAcceptResult.Rejected
        return acceptLookup(block, opener)
    }

    private fun acceptLookup(block: PsiBlockBuilder, scope: PsiScope.Lookup): BlockAcceptResult = when (block) {
        is ContentBuilder -> {
            val lookup = scope.lookup
            val split = if (lookup == null) consumeWordDefault(block.text)
            else consumeWordCustom(block.text, lookup.unquote(), scope.plus != null)
            consumeContentSplit(block, split)
        }
        is CommentBuilder -> {
            val lookup = scope.lookup
            val split = if (lookup == null) consumeWordDefault(block.text)
            else consumeWordCustom(block.text, lookup.unquote(), scope.plus != null)
            consumeCommentSplit(block, split)
        }
        else -> BlockAcceptResult.Rejected
    }

    private fun consumeFinal(block: PsiBlockBuilder): BlockAcceptResult {
        satisfied = true
        entries.merge(block)
        return BlockAcceptResult.ConsumedFinal
    }

    private fun consumeContentSplit(block: ContentBuilder, split: Int) : BlockAcceptResult = when(split) {
        -1 -> entries.merge(block)
        0 -> BlockAcceptResult.Rejected.also { satisfied = true }
        block.length  -> consumeFinal(block)
        else -> {
            satisfied = true
            entries.merge(block.copy(end = split, text = block.text.substring(0, split)))
            BlockAcceptResult.ConsumedPartial(block.copy(start = split, text = block.text.substring(split)))
        }
    }

    private fun consumeCommentSplit(block: CommentBuilder, split: Int) : BlockAcceptResult = when(split) {
        -1 -> entries.merge(block)
        block.length -> consumeFinal(block)
        else -> {
            satisfied = true
            entries += block.copy(end = split)
            BlockAcceptResult.ConsumedPartial(block.copy(start = split))
        }
    }
}

private fun MutableList<PsiBlockBuilder>.merge(block: PsiBlockBuilder): BlockAcceptResult = when (val it = lastOrNull()) {
    is ContentBuilder if (block is ContentBuilder) -> this[lastIndex] = ContentBuilder(
        min(it.start, block.start),
        max(it.end, block.end),
        it.text + block.text
    )
    else -> this += block
}.let {
    BlockAcceptResult.ConsumedOpen
}

private fun consumeLine(str: String, immediate: Boolean): Int {
    var state = 0
    val index = str.countWhile { ch ->
        matchLineChar(ch, state, immediate).also { if (it >= 0) state = it } >= 0
    }
    // -1 indicates we didn't reach a newline
    return if (index == str.length && state == 0) -1 else index
}

private fun matchLineChar(ch: Char, state: Int, immediate: Boolean): Int = when (state) {
    0 -> when (ch) {
        in WORD_BREAKS -> 0
        in LINE_BREAKS -> if (immediate) 1 else 0
        else -> 1
    }

    1 -> when (ch) {
        '\n' -> 2
        '\r' -> 3
        else -> 1
    }

    2 -> when (ch) {
        '\r' -> 3
        else -> -1
    }

    else -> -1
}

private fun consumeWordDefault(str: String): Int = when (val idx = str.countMatching(*WHITESPACES)) {
    str.length -> -1
    else -> idx + str.countWhile(idx) { it !in WHITESPACES }
}

private fun consumeWordCustom(str: String, match: String, capturing: Boolean): Int = when (val idx = str.indexOf(match)) {
    -1 -> -1
    else if capturing -> idx + match.length
    else -> idx
}

private fun PsiBlockBuilder?.isNotBlank(): Boolean = when (this) {
    is ContentBuilder -> text.isNotBlank()
    is CommentBuilder -> text.isNotBlank()
    null -> false
    else -> true
}
