package dev.kikugie.stonecutter.intellij.action

import com.intellij.codeInsight.hint.HintManager
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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.StonecutterBundle.BUNDLE
import dev.kikugie.stonecutter.intellij.action.StitcherWrapAction.ActionResult.Err
import dev.kikugie.stonecutter.intellij.action.StitcherWrapAction.ActionResult.Ok
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.util.commenter
import dev.kikugie.stonecutter.intellij.lang.util.nextNotEmptyLeaf
import dev.kikugie.stonecutter.intellij.lang.util.prevNotEmptyLeaf
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.PropertyKey

abstract class StitcherWrapAction(protected val name: @Command String) : AnAction(), DumbAware {
    abstract fun isApplicableIn(editor: Editor, file: PsiFile): Boolean
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
        object Ok: ActionResult
        class Err(val msg: @PropertyKey(resourceBundle = BUNDLE) String) : ActionResult
    }
}

class NewConditionAction : StitcherWrapAction("Wrap in Condition") {
    override fun isApplicableIn(editor: Editor, file: PsiFile): Boolean {
        if (file is StitcherFile || file.commenter == null) return false

        return true
    }

    override fun performWriteAction(editor: Editor, file: PsiFile): ActionResult {
        val commenter = file.commenter
            ?: return Err("stonecutter.action.wrap.err_no_commenter")
        val ctx = SelectionContext(editor, file)
            ?: return Err("stonecutter.action.wrap.err_no_psi")

        return when {
            ctx.first.prevNotEmptyLeaf !is PsiWhiteSpace
                || ctx.last.nextNotEmptyLeaf !is PsiWhiteSpace -> ctx.wrapInline(commenter)
            ctx.startLineIndex == ctx.endLineIndex -> ctx.wrapSingleLine(commenter)
            else -> ctx.wrapMultiLine(commenter)
        }
    }

    private fun SelectionContext.wrapMultiLine(commenter: Commenter): ActionResult {
        val opener = commenter.wrapLine(SCOPED_OPENER)
        val closer = commenter.wrapLine(SCOPED_CLOSER)
        if (opener == null || closer == null)
            return Err("stonecutter.action.wrap.err_no_commenter")

        val lineStartOffset = document.getLineStartOffset(startLineIndex)
        val indent = document[lineStartOffset, first.startOffset]
        val caret = first.startOffset + opener.indexOf('{') - 1
        return insertAround("$opener\n$indent", "\n$indent$closer", caret)
    }

    private fun SelectionContext.wrapSingleLine(commenter: Commenter): ActionResult {
        val opener = commenter.wrapLine(LINE_OPENER)
            ?: return Err("stonecutter.action.wrap.err_no_commenter")

        val lineStartOffset = document.getLineStartOffset(startLineIndex)
        val indent = document[lineStartOffset, first.startOffset]
        val caret = first.startOffset + opener.length
        return insertAround("$opener\n$indent", null, caret)
    }

    private fun SelectionContext.wrapInline(commenter: Commenter): ActionResult {
        val prefix = commenter.blockCommentPrefix
        val suffix = commenter.blockCommentSuffix
        if (prefix == null || suffix == null)
            return Err("stonecutter.action.wrap.err_no_inline_commenter")

        val caret = first.startOffset + prefix.length + SCOPED_OPENER.indexOf('{') - 1
        return insertAround("$prefix$SCOPED_OPENER$suffix", "$prefix$SCOPED_CLOSER$suffix", caret)
    }

    private companion object {
        @Language("Stitcher") const val SCOPED_OPENER: String = "? if  {"
        @Language("Stitcher") const val SCOPED_CLOSER: String = "?}"

        @Language("Stitcher") const val LINE_OPENER: String = "? if "
    }
}

private class SelectionContext(val editor: Editor, val first: PsiElement, val last: PsiElement) {
    val selection: SelectionModel inline get() = editor.selectionModel
    val document: Document inline get() = editor.document

    val startLineIndex by lazy { document.getLineNumber(first.startOffset) }
    val endLineIndex by lazy { document.getLineNumber(last.endOffset) }

    fun insertAround(prefix: String?, suffix: String?, caret: Int): StitcherWrapAction.ActionResult {
        if (suffix != null) document.insertString(last.endOffset, suffix)
        if (prefix != null) document.insertString(first.startOffset, prefix)
        if (caret >= 0) editor.caretModel.moveToOffset(caret)
        editor.selectionModel.removeSelection()
        return Ok
    }

    companion object {
        operator fun invoke(editor: Editor, file: PsiFile): SelectionContext? {
            val sel = editor.selectionModel
            val first = file.findNext(sel.selectionStart, file.findElementAt(sel.selectionStart))
            val last = file.findPrev(sel.selectionEnd, file.findElementAt(sel.findSelectionEnd()))
            return SelectionContext(editor, first ?: return null, last ?: return null)
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

private fun Commenter.wrapLine(text: String): String? = when {
    lineCommentPrefix != null -> "$lineCommentPrefix$text"
    blockCommentPrefix != null && blockCommentSuffix != null -> "$blockCommentPrefix$text$blockCommentSuffix"
    else -> null
}

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Document.get(start: Int, end: Int): String =
    getText(TextRange(start, end))
