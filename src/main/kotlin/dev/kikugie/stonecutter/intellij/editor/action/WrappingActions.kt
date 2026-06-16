package dev.kikugie.stonecutter.intellij.editor.action

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.template.TemplateBuilder
import com.intellij.codeInsight.template.TemplateBuilderFactory
import com.intellij.lang.Commenter
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.NlsContexts.Command
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import dev.kikugie.commons.collections.lastNotNullOfOrNull
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.StonecutterBundle.BUNDLE
import dev.kikugie.stonecutter.intellij.editor.action.StitcherWrapAction.ActionResult
import dev.kikugie.stonecutter.intellij.editor.action.StitcherWrapAction.ActionResult.Err
import dev.kikugie.stonecutter.intellij.editor.action.StitcherWrapAction.ActionResult.Ok
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.psi.*
import dev.kikugie.stonecutter.intellij.lang.psi.PsiDefinition.Kind
import dev.kikugie.stonecutter.intellij.lang.util.*
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.PropertyKey
import kotlin.text.iterator

abstract class StitcherWrapAction(protected val name: @Command String) : AnAction(), DumbAware {
    open fun isApplicableIn(editor: Editor, file: PsiFile): Boolean =
        file !is StitcherFile && file.commenter != null

    abstract fun performWriteAction(editor: Editor, file: PsiFile): ActionResult

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        val file = event.getData(CommonDataKeys.PSI_FILE)
        event.presentation.isEnabledAndVisible = editor != null && file != null && isApplicableIn(editor, file)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val file = event.getData(CommonDataKeys.PSI_FILE) ?: return
        val project = event.project ?: return
        if (!editor.selectionModel.hasSelection()) return

        val action = Runnable { handleActionResult(performWriteAction(editor, file), editor) }
        WriteCommandAction.runWriteCommandAction(project, name, "Stonecutter", action, file)
    }

    private fun handleActionResult(result: ActionResult, editor: Editor) {
        if (result !is Err) return

        val message = StonecutterBundle.message(result.msg)
        HintManager.getInstance().showErrorHint(editor, message)
    }

    sealed interface ActionResult {
        object Ok : ActionResult
        class Err(val msg: @PropertyKey(resourceBundle = BUNDLE) String) : ActionResult
    }
}

class NewConditionAction : StitcherWrapAction("Wrap in Condition") {
    override fun performWriteAction(editor: Editor, file: PsiFile): ActionResult {
        val commenter = file.commenter
            ?: return Err("stonecutter.action.wrap.err_no_commenter")
        val ctx = SelectionContext(editor, file)

        val builder = TemplateBuilderFactory.getInstance()
            .createTemplateBuilder(file)
        return when {
            ctx.isInline -> ctx.wrapInline(builder, commenter)
            ctx.isMultiLine -> ctx.wrapMultiLine(builder, commenter)
            else -> ctx.wrapSingleLine(builder, commenter)
        }
    }

    private fun SelectionContext.wrapInline(template: TemplateBuilder, commenter: Commenter): ActionResult {
        val pr = commenter.blockCommentPrefix
        val sf = commenter.blockCommentSuffix
        if (pr == null || sf == null)
            return Err("stonecutter.action.wrap.err_no_inline_commenter")

        document.insertString(endOffset, "$pr$COND_CLOSER$sf")
        document.insertString(startOffset, "$pr$COND_OPENER$sf")
        selection.removeSelection()
        return template.insertAt(startOffset + pr.length + 5, "")
    }

    private fun SelectionContext.wrapMultiLine(template: TemplateBuilder, commenter: Commenter): ActionResult {
        val (pr, sf) = commenter.getLineSurrounders()
            ?: return Err("stonecutter.action.wrap.err_no_commenter")

        val indent = document.findIndentAt(startLineIndex)
        document.insertString(endOffset, "\n$indent$pr$COND_CLOSER$sf")
        document.insertString(startOffset, "$pr$COND_OPENER$sf\n$indent")
        selection.removeSelection()
        return template.insertAt(startOffset + pr.length + 5, "")
    }

    private fun SelectionContext.wrapSingleLine(template: TemplateBuilder, commenter: Commenter): ActionResult {
        val (pr, sf) = commenter.getLineSurrounders()
            ?: return Err("stonecutter.action.wrap.err_no_commenter")

        val indent = document.findIndentAt(startLineIndex)
        document.insertString(startOffset, "$pr$COND_LINE$sf\n$indent")
        selection.removeSelection()
        return template.insertAt(startOffset + pr.length + 5, "")
    }

    private companion object {
        @Language("Stitcher") const val COND_OPENER = "? if  {"
        @Language("Stitcher") const val COND_CLOSER = "?}"
        @Language("Stitcher") const val COND_LINE = "? if "
    }
}

class ExtendConditionAction : StitcherWrapAction("Wrap in Extension") {
    override fun performWriteAction(editor: Editor, file: PsiFile): ActionResult {
        val commenter = file.commenter
            ?: return Err("stonecutter.action.wrap.err_no_commenter")
        val ctx = SelectionContext(editor, file)

        val candidate = file.getStitcherAst()
            ?.accept(ExtensionTargetLocator(ctx.startOffset, ctx.endOffset))
            ?: return Err("stonecutter.action.wrap.err_nothing_to_extend")

        val builder = TemplateBuilderFactory.getInstance()
            .createTemplateBuilder(file)
        return ctx.extendCodeBlock(candidate, builder, commenter)
    }

    private fun SelectionContext.extendCodeBlock(code: PsiBlock.Code, template: TemplateBuilder, commenter: Commenter): ActionResult {
        val definition = code.definition
            ?: return Err("stonecutter.action.wrap.err_nothing_to_extend")

        return when {
            definition.kind == Kind.CLOSER -> extendClosedScope(code, template, commenter)
            definition.opener == null -> extendLineScope(code, template, commenter)
            // TODO: It's supposed to be able to split the condition, but idk how yet
            else -> Err("stonecutter.action.wrap.err_nothing_to_extend")
        }
    }

    private fun SelectionContext.extendLineScope(code: PsiBlock.Code, template: TemplateBuilder, commenter: Commenter): ActionResult {
        val result = when {
            isInline -> wrapInlineExtension(template, commenter)
            isMultiLine -> wrapMultiLineExtension(template, commenter)
            else -> wrapSingleLineExtension(template, commenter)
        }
        if (result is Err) return result

        document.insertString(code.hostComment?.element!!.findPrefixOffset(), " {")
        return Ok
    }

    private fun SelectionContext.extendClosedScope(code: PsiBlock.Code, template: TemplateBuilder, commenter: Commenter): ActionResult {
        val result = when {
            isInline -> wrapInlineCloser(commenter)
            else -> wrapMultiLineCloser(commenter)
        }
        if (result is Err) return result

        val offset = code.hostComment?.element!!.findPrefixOffset()
        document.insertString(offset, "  {")
        return template.insertAt(offset + 1, "else")
    }

    private fun SelectionContext.wrapInlineExtension(template: TemplateBuilder, commenter: Commenter): ActionResult {
        val pr = commenter.blockCommentPrefix
        val sf = commenter.blockCommentSuffix
        if (pr == null || sf == null)
            return Err("stonecutter.action.wrap.err_no_inline_commenter")

        document.insertString(endOffset, "$pr$COND_CLOSER$sf")
        document.insertString(startOffset, "$pr$COND_EXTENSION$sf")
        selection.removeSelection()
        return template.insertAt(startOffset + pr.length + 3, "else")
    }

    private fun SelectionContext.wrapMultiLineExtension(template: TemplateBuilder, commenter: Commenter): ActionResult {
        val (pr, sf) = commenter.getLineSurrounders()
            ?: return Err("stonecutter.action.wrap.err_no_commenter")

        val indent = document.findIndentAt(startLineIndex)
        document.insertString(endOffset, "\n$indent$pr$COND_CLOSER$sf")
        document.insertString(startOffset, "$pr$COND_EXTENSION$sf\n$indent")
        selection.removeSelection()
        return template.insertAt(startOffset + pr.length + 3, "else")
    }

    private fun SelectionContext.wrapSingleLineExtension(template: TemplateBuilder, commenter: Commenter): ActionResult {
        val (pr, sf) = commenter.getLineSurrounders()
            ?: return Err("stonecutter.action.wrap.err_no_commenter")

        val indent = document.findIndentAt(startLineIndex)
        document.insertString(startOffset, "$pr$COND_LINE$sf\n$indent")
        selection.removeSelection()
        return template.insertAt(startOffset + pr.length + 3, "else")
    }

    private fun SelectionContext.wrapInlineCloser(commenter: Commenter): ActionResult {
        val pr = commenter.blockCommentPrefix
        val sf = commenter.blockCommentSuffix
        if (pr == null || sf == null)
            return Err("stonecutter.action.wrap.err_no_inline_commenter")

        document.insertString(endOffset, "$pr$COND_CLOSER$sf")
        selection.removeSelection()
        return Ok
    }

    private fun SelectionContext.wrapMultiLineCloser(commenter: Commenter): ActionResult {
        val (pr, sf) = commenter.getLineSurrounders()
            ?: return Err("stonecutter.action.wrap.err_no_commenter")

        val indent = document.findIndentAt(startLineIndex)
        document.insertString(endOffset, "\n$indent$pr$COND_CLOSER$sf")
        selection.removeSelection()
        return Ok
    }

    private class ExtensionTargetLocator(val start: Int, val end: Int) : PsiBlock.Visitor<PsiBlock.Code?> {
        override fun visitContent(content: PsiBlock.Content): PsiBlock.Code? = null
        override fun visitComment(comment: PsiBlock.Comment): PsiBlock.Code? = null

        override fun visitRoot(root: PsiBlock.Root): PsiBlock.Code? =
            root.entries.lastNotNullOfOrNull { it.accept(this) }

        override fun visitCode(code: PsiBlock.Code): PsiBlock.Code? =
            code.entries.lastNotNullOfOrNull { it.accept(this) }
                ?: code.takeIf(::canBeExtended)

        private fun canBeExtended(code: PsiBlock.Code): Boolean {
            // Only conditions may be extended
            val definition = code.definition as? PsiCondition ?: return false

            // Lookup conditions can't be split or extended
            if (definition.opener is PsiScope.Lookup) return false

            val sibling = code.nextSibling as? PsiBlock.Content ?: return false
            return start >= sibling.startOffset
                && start < sibling.endOffset
                && sibling.text.take(start - sibling.startOffset).isBlank()
        }
    }

    private companion object {
        @Language("Stitcher") const val COND_EXTENSION = "?}  {"
        @Language("Stitcher") const val COND_LINE = "?} "
        @Language("Stitcher") const val COND_CLOSER = "?}"
    }
}

class NewLocalReplacementAction : StitcherWrapAction("Wrap in Replacement") {
    override fun performWriteAction(editor: Editor, file: PsiFile): ActionResult {
        val commenter = file.commenter
            ?: return Err("stonecutter.action.wrap.err_no_commenter")
        val ctx = SelectionContext(editor, file)

        val builder = TemplateBuilderFactory.getInstance()
            .createTemplateBuilder(file)
        return when {
            ctx.isInline -> ctx.wrapInline(builder, commenter)
            ctx.isMultiLine -> ctx.wrapMultiLine(builder, commenter)
            else -> ctx.wrapSingleLine(builder, commenter)
        }
    }

    private fun SelectionContext.wrapInline(template: TemplateBuilder, commenter: Commenter): ActionResult {
        val pr = commenter.blockCommentPrefix
        val sf = commenter.blockCommentSuffix
        if (pr == null || sf == null)
            return Err("stonecutter.action.wrap.err_no_inline_commenter")

        document.insertString(endOffset, "$pr$REPL_CLOSER$sf")
        document.insertString(startOffset, "$pr$REPL_OPENER$sf")
        selection.removeSelection()
        template.insertAt(startOffset + pr.length + 5, "", false)
        template.insertAt(startOffset + pr.length + 7, "", false)
        return template.insertAt(startOffset + pr.length + 13, "")
    }

    private fun SelectionContext.wrapMultiLine(template: TemplateBuilder, commenter: Commenter): ActionResult {
        val (pr, sf) = commenter.getLineSurrounders()
            ?: return Err("stonecutter.action.wrap.err_no_commenter")

        val indent = document.findIndentAt(startLineIndex)
        document.insertString(endOffset, "\n$indent$pr$REPL_CLOSER$sf")
        document.insertString(startOffset, "$pr$REPL_OPENER$sf\n$indent")
        selection.removeSelection()
        template.insertAt(startOffset + pr.length + 5, "", false)
        template.insertAt(startOffset + pr.length + 7, "", false)
        return template.insertAt(startOffset + pr.length + 13, "")
    }

    private fun SelectionContext.wrapSingleLine(template: TemplateBuilder, commenter: Commenter): ActionResult {
        val (pr, sf) = commenter.getLineSurrounders()
            ?: return Err("stonecutter.action.wrap.err_no_commenter")

        val indent = document.findIndentAt(startLineIndex)
        document.insertString(startOffset, "$pr$REPL_LINE$sf\n$indent")
        selection.removeSelection()
        template.insertAt(startOffset + pr.length + 5, "", false)
        template.insertAt(startOffset + pr.length + 7, "", false)
        return template.insertAt(startOffset + pr.length + 13, "")
    }

    private companion object {
        @Language("Stitcher") const val REPL_OPENER = "~ if  '' -> '' {"
        @Language("Stitcher") const val REPL_CLOSER = "~}"
        @Language("Stitcher") const val REPL_LINE = "~ if  '' -> ''"
    }
}

private class SelectionContext(val editor: Editor, val first: PsiElement?, val last: PsiElement?) {
    val selection: SelectionModel inline get() = editor.selectionModel
    val document: Document inline get() = editor.document

    val startOffset: Int = first?.startOffset ?: selection.selectionStart
    val endOffset: Int = last?.endOffset ?: selection.findSelectionEnd()

    val startLineIndex by lazy { document.getLineNumber(startOffset) }
    val endLineIndex by lazy { document.getLineNumber(endOffset) }

    // Fixme check if it's a line break
    val isInline: Boolean
        get() = last != null && last.nextNotEmptyLeaf !is PsiWhiteSpace

    val isMultiLine: Boolean
        get() = startLineIndex != endLineIndex

    fun insertAround(prefix: String?, suffix: String?, caret: Int): ActionResult {
        if (suffix != null) document.insertString(endOffset, suffix)
        if (prefix != null) document.insertString(startOffset, prefix)
        if (caret >= 0) editor.caretModel.moveToOffset(caret)
        editor.selectionModel.removeSelection()
        return Ok
    }

    companion object {
        operator fun invoke(editor: Editor, file: PsiFile): SelectionContext {
            val sel = editor.selectionModel
            val first = file.findNext(sel.selectionStart, file.findElementAt(sel.selectionStart))
            val last = file.findPrev(sel.selectionEnd, file.findElementAt(sel.findSelectionEnd()))
            return SelectionContext(editor, first, last)
        }

        private tailrec fun PsiFile.findNext(start: Int, element: PsiElement?): PsiElement? = when (element) {
            is PsiWhiteSpace -> findNext(start, element.nextNotEmptyLeaf)
            else -> element
        }

        private tailrec fun PsiFile.findPrev(end: Int, element: PsiElement?): PsiElement? = when (element) {
            is PsiWhiteSpace -> findPrev(end, element.prevNotEmptyLeaf)
            else -> element
        }

        private fun SelectionModel.findSelectionEnd(): Int = maxOf(selectionStart, selectionEnd - 1)
    }
}

context(ctx: SelectionContext)
private fun TemplateBuilder.insertAt(index: Int, str: String, run: Boolean = true): ActionResult {
    replaceRange(TextRange(index, index), str)
    if (run) run(ctx.editor, true)
    return Ok
}

private fun Commenter.getLineSurrounders(): Pair<String, String>? {
    lineCommentPrefix?.let { return it to "" }
    blockCommentPrefix?.let { pr -> blockCommentSuffix?.let { return pr to it } }
    return null
}

private fun Document.findIndentAt(line: Int): String {
    val start = getLineStartOffset(line)
    val end = getLineEndOffset(line)
    for (i in start..<end) when (charsSequence[i]) {
        ' ', '\t' -> continue
        else -> return charsSequence.substring(start, i)
    }
    return charsSequence.substring(start, end)
}

private fun PsiComment.findPrefixOffset(): Int {
    var position = innerRange.endOffset
    for (char in innerText.reversed()) when (char) {
        ' ', '\t' -> position -= 1
        else -> break
    }
    return startOffset + position
}
