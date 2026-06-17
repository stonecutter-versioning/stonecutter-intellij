package dev.kikugie.stonecutter.intellij.editor.template

import com.intellij.codeInsight.template.CustomLiveTemplateBase
import com.intellij.codeInsight.template.CustomTemplateCallback
import com.intellij.codeInsight.template.TemplateBuilder
import com.intellij.codeInsight.template.TemplateBuilderFactory
import com.intellij.codeInsight.template.impl.CustomLiveTemplateLookupElement
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.injected.editor.EditorWindow
import com.intellij.lang.Commenter
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsActions.ActionText
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import dev.kikugie.commons.takeAs
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.StonecutterBundle.BUNDLE
import dev.kikugie.stonecutter.intellij.editor.template.StitcherLiveTemplate.InjectedTemplateContext
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.util.commenter
import dev.kikugie.stonecutter.intellij.lang.util.containingComment
import dev.kikugie.stonecutter.intellij.lang.util.contentRange
import dev.kikugie.stonecutter.intellij.lang.util.innerText
import dev.kikugie.stonecutter.intellij.util.findIndentAt
import org.jetbrains.annotations.PropertyKey

// FIXME: expanding incomplete conditions can select incorrect one
abstract class StitcherLiveTemplate(
    val key: String,
    val name: @PropertyKey(resourceBundle = BUNDLE) String,
    val text: @PropertyKey(resourceBundle = BUNDLE) String = "$name.sample",
) : CustomLiveTemplateBase() {
    init {
        LOOKUP[key] = this
    }

    abstract fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean
    abstract fun doExpand(ctx: InjectedTemplateContext)

    override fun supportsWrapping(): Boolean = false
    override fun supportsMultiCaret(): Boolean = false
    override fun getTitle(): @ActionText String = StonecutterBundle.message(name)
    override fun getShortcut(): Char = TemplateSettings.getInstance().defaultShortcutChar
    override fun computeTemplateKey(callback: CustomTemplateCallback): String? = key

    override fun isApplicable(callback: CustomTemplateCallback, offset: Int, wrapping: Boolean): Boolean =
        !wrapping && isApplicable(callback.file, callback.editor, offset)

    override fun hasCompletionItem(callback: CustomTemplateCallback, offset: Int): Boolean =
        isApplicable(callback.file, callback.editor, offset)

    override fun getLookupElements(file: PsiFile, editor: Editor, offset: Int): Collection<CustomLiveTemplateLookupElement> =
        if (!isApplicable(file, editor, offset)) emptyList() else listOf(makeLookupElement())

    override fun wrap(selection: String, callback: CustomTemplateCallback): Nothing =
        throw UnsupportedOperationException()

    override fun expand(key: String, callback: CustomTemplateCallback) {
        if (callback.file !is StitcherFile) return
        val ctx = InjectedTemplateContext(callback)
        findTemplate(ctx).doExpand(ctx)
    }

    private fun makeLookupElement(): CustomLiveTemplateLookupElement = CustomLiveTemplateLookupElement(
        this, key, key, StonecutterBundle.message(text), true, true
    )

    // Not unexpectedly, IntelliJ is dumb and picks the wrong template for the prefix
    private fun findTemplate(ctx: InjectedTemplateContext): StitcherLiveTemplate {
        val suffix = ctx.comment.innerText.substring(1, ctx.callback.offset + 1).trim()
        return LOOKUP.getOrElse(suffix) { this }
    }

    class InjectedTemplateContext(val callback: CustomTemplateCallback) {
        val file: PsiFile = InjectedLanguageManager.getInstance(callback.project).getTopLevelFile(callback.file)
        val comment: PsiComment = callback.file.takeAs<StitcherFile>().containingComment
        val document: Document = file.fileDocument
        val offset: Int = comment.contentRange.startOffset + callback.offset
        val template: TemplateBuilder by lazy { TemplateBuilderFactory.getInstance().createTemplateBuilder(file) }

        val project: Project inline get() = callback.project
        val editor: Editor inline get() = callback.editor
    }

    private companion object {
        val LOOKUP: MutableMap<String, StitcherLiveTemplate> = mutableMapOf()
    }
}

class LineConditionTemplate : StitcherLiveTemplate("il", "stonecutter.template.line_condition") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '?')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "? if ")
        template.insertAt(mark.endOffset, "")
        completeLineScope(mark)
        template.run(editor.jailbreak(), true)
    }
}

class ScopedConditionTemplate : StitcherLiveTemplate("ic", "stonecutter.template.closed_condition") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '?')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "? if  {")
        template.insertAt(mark.endOffset - 2, "")
        completeBlockScope(mark, '?')
        template.run(editor.jailbreak(), true)
    }
}

class LookupConditionTemplate : StitcherLiveTemplate("is", "stonecutter.template.lookup_condition") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '?')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "? if  >> ''")
        template.insertAt(mark.endOffset - 6, "")
        template.insertAt(mark.endOffset - 1, "")
        completeLineScope(mark)
        template.run(editor.jailbreak(), true)
    }
}

class LineExtensionTemplate : StitcherLiveTemplate("el", "stonecutter.template.line_extension") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '?')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "?} else")
        template.insertAt(mark.endOffset, "")
        completeLineScope(mark)
        template.run(editor.jailbreak(), true)
    }
}

class ScopedExtensionTemplate : StitcherLiveTemplate("ec", "stonecutter.template.closed_extension") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '?')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "?} else {")
        template.insertAt(mark.endOffset - 2, "")
        completeBlockScope(mark, '?')
        template.run(editor.jailbreak(), true)
    }
}

class LookupExtensionTemplate : StitcherLiveTemplate("es", "stonecutter.template.lookup_extension") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '?')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "?} else if  >> ''")
        template.insertAt(mark.endOffset - 6, "")
        template.insertAt(mark.endOffset - 1, "")
        completeLineScope(mark)
        template.run(editor.jailbreak(), true)
    }
}

class LineSwapTemplate : StitcherLiveTemplate("sl", "stonecutter.template.line_swap") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '$')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "$ ")
        template.insertAt(mark.endOffset, "")
        completeLineScope(mark)
        template.run(editor.jailbreak(), true)
    }
}

class ScopedSwapTemplate : StitcherLiveTemplate("sc", "stonecutter.template.closed_swap") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '$')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "$  {")
        template.insertAt(mark.endOffset - 2, "")
        completeBlockScope(mark, '$')
        template.run(editor.jailbreak(), true)
    }
}

class LookupSwapTemplate : StitcherLiveTemplate("ss", "stonecutter.template.lookup_swap") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '$')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "$  >> ''")
        template.insertAt(mark.endOffset - 6, "")
        template.insertAt(mark.endOffset - 1, "")
        completeLineScope(mark)
        template.run(editor.jailbreak(), true)
    }
}

class LineReplacementTemplate : StitcherLiveTemplate("rl", "stonecutter.template.line_replacement") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '~')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "~ if  '' -> ''")
        template.insertAt(mark.endOffset - 9, "")
        template.insertAt(mark.endOffset - 7, "")
        template.insertAt(mark.endOffset - 1, "")
        completeLineScope(mark)
        template.run(editor.jailbreak(), true)
    }
}

class ScopedReplacementTemplate : StitcherLiveTemplate("rc", "stonecutter.template.closed_replacement") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '~')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "~ if  '' -> '' {")
        template.insertAt(mark.endOffset - 11, "")
        template.insertAt(mark.endOffset - 9, "")
        template.insertAt(mark.endOffset - 3, "")
        completeBlockScope(mark, '~')
        template.run(editor.jailbreak(), true)
    }
}

class LookupReplacementTemplate : StitcherLiveTemplate("rs", "stonecutter.template.lookup_replacement") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '~')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "~ if  '' -> '' >> ''")
        template.insertAt(mark.endOffset - 15, "")
        template.insertAt(mark.endOffset - 13, "")
        template.insertAt(mark.endOffset - 7, "")
        template.insertAt(mark.endOffset - 1, "")
        completeLineScope(mark)
        template.run(editor.jailbreak(), true)
    }
}

class NamedReplacementTemplate : StitcherLiveTemplate("rn", "stonecutter.template.named_replacement") {
    override fun isApplicable(file: PsiFile, editor: Editor, offset: Int): Boolean =
        isEmptyDefinitionOf(file, offset, '~')

    override fun doExpand(ctx: InjectedTemplateContext): Unit = with(ctx) {
        val mark = document.createRangeMarker(comment.contentRange.startOffset, offset + 1)
            .apply { isGreedyToRight = true }

        document.replaceString(mark.textRange, "~ if  '' -> '' as ")
        template.insertAt(mark.endOffset - 13, "")
        template.insertAt(mark.endOffset - 11, "")
        template.insertAt(mark.endOffset - 5, "")
        template.insertAt(mark.endOffset, "")
        completeLineScope(mark)
        template.run(editor.jailbreak(), true)
    }
}

private fun StitcherLiveTemplate.isEmptyDefinitionOf(file: PsiFile, offset: Int, char: Char): Boolean {
    val file = file as? StitcherFile ?: return false
    val contents = file.containingComment.innerText

    // FIXME: Expanding a template without a space after the marker fucks it up
    if (!contents.startsWith("$char ")) return false

    val trimmed = contents.substring(1, offset.coerceAtLeast(1)).trim()
    return trimmed.isEmpty() || key.startsWith(trimmed)
}

private fun InjectedTemplateContext.completeLineScope(mark: RangeMarker) {
    val commenter = file.commenter ?: return
    val opener = document[comment.startOffset, comment.contentRange.startOffset]

    val string =
        if (opener !in commenter.lineCommentPrefixes) commenter.blockCommentSuffix ?: return
        else "\n${document.findIndentAt(comment.startOffset)}"

    document.insertString(mark.endOffset, string)
    template.insertAt(mark.endOffset, "")
}

private fun InjectedTemplateContext.completeBlockScope(mark: RangeMarker, ch: Char) {
    val commenter = file.commenter ?: return
    val opener = document[comment.startOffset, comment.contentRange.startOffset]

    if (opener in commenter.lineCommentPrefixes) continueLine(mark, commenter, ch)
    else continueBlock(mark, commenter, ch)
}

private fun InjectedTemplateContext.continueLine(mark: RangeMarker, commenter: Commenter, ch: Char) {
    val indent = document.findIndentAt(comment.startOffset)
    document.insertString(mark.endOffset, "\n$indent")
    template.insertAt(mark.endOffset, "")

    document.insertString(mark.endOffset, "\n$indent${commenter.lineCommentPrefix}$ch}")
}

private fun InjectedTemplateContext.continueBlock(mark: RangeMarker, commenter: Commenter, ch: Char) {
    val pr = commenter.blockCommentPrefix ?: return
    val sf = commenter.blockCommentSuffix ?: return
    document.insertString(mark.endOffset, sf)
    template.insertAt(mark.endOffset, "")

    document.insertString(mark.endOffset, "$pr$ch}$sf")
}

private fun TemplateBuilder.insertAt(index: Int, str: String): Unit =
    replaceRange(TextRange(index, index), str)

private fun Document.replaceString(range: TextRange, str: String): Int {
    val diff = str.length - (range.endOffset - range.startOffset)
    replaceString(range.startOffset, range.endOffset, str)
    return diff
}

private operator fun Document.get(start: Int, end: Int): String = charsSequence
    .run { substring(start.coerceAtLeast(0), end.coerceAtMost(length)) }

private tailrec fun Editor.jailbreak(): Editor = when (this) {
    is EditorWindow -> delegate.jailbreak()
    else -> this
}
