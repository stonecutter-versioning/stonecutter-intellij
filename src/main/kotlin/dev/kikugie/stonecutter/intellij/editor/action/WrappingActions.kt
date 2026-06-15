package dev.kikugie.stonecutter.intellij.editor.action

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
        object Ok : ActionResult
        class Err(val msg: @PropertyKey(resourceBundle = BUNDLE) String) : ActionResult
    }
}

class NewConditionAction : StitcherWrapAction("Wrap in Condition") {
    override fun isApplicableIn(editor: Editor, file: PsiFile): Boolean =
        file !is StitcherFile && file.commenter != null

    override fun performWriteAction(editor: Editor, file: PsiFile): ActionResult {
        val commenter = file.commenter
            ?: return Err("stonecutter.action.wrap.err_no_commenter")
        val ctx = SelectionContext(editor, file)
            ?: return Err("stonecutter.action.wrap.err_no_psi")

        return when {
            ctx.isInline -> ctx.wrapInLine(commenter, "? if  {", "?}", openerCaret = 5)
            ctx.isMultiLine -> ctx.wrapAroundLine(commenter, "? if  {", "?}", openerCaret = 5)
            else -> ctx.wrapAroundLine(commenter, opener = "? if ", openerCaret = 5)
        }
    }
}

class ExtendConditionAction : StitcherWrapAction("Wrap in Extension") {
    override fun isApplicableIn(editor: Editor, file: PsiFile): Boolean =
        file !is StitcherFile && file.commenter != null

    override fun performWriteAction(editor: Editor, file: PsiFile): ActionResult {
        val commenter = file.commenter
            ?: return Err("stonecutter.action.wrap.err_no_commenter")
        val ctx = SelectionContext(editor, file)
            ?: return Err("stonecutter.action.wrap.err_no_psi")

        val candidate = file.getStitcherAst()
            ?.accept(ExtensionTargetLocator(ctx.first.startOffset, ctx.last.endOffset))
            ?: return Err("stonecutter.action.wrap.err_nothing_to_extend")

        return ctx.extendCodeBlock(candidate, commenter)
    }

    private fun SelectionContext.extendCodeBlock(code: PsiBlock.Code, commenter: Commenter): ActionResult {
        val definition = code.definition
            ?: return Err("stonecutter.action.wrap.err_nothing_to_extend")

        return when {
            definition.kind == Kind.CLOSER -> extendClosedScope(code, commenter)
            definition.opener == null -> extendLineScope(code, commenter)
            // TODO: It's supposed to be able to split the condition, but idk how yet
            else -> Err("stonecutter.action.wrap.err_nothing_to_extend")
        }
    }

    private fun SelectionContext.extendLineScope(code: PsiBlock.Code, commenter: Commenter): ActionResult {
        val result = when {
            isInline -> wrapInLine(commenter, "?} else {", "?}", openerCaret = 7)
            isMultiLine -> wrapAroundLine(commenter, "?} else {", "?}", openerCaret = 7)
            else -> wrapAroundLine(commenter, opener = "?} else", openerCaret = 7)
        }
        if (result is Err) return result

        document.insertString(code.hostComment?.element!!.findPrefixOffset(), " {")
        return Ok
    }

    private fun SelectionContext.extendClosedScope(code: PsiBlock.Code, commenter: Commenter): ActionResult {
        val result = when {
            isInline -> wrapInLine(commenter, closer = "?}")
            else -> wrapAroundLine(commenter, closer = "?}")
        }
        if (result is Err) return result

        val offset = code.hostComment?.element!!.findPrefixOffset()
        document.insertString(offset, " else {")
        editor.caretModel.moveToOffset(offset + 5)
        return Ok
    }

    private fun SelectionContext.extendSplitScope(code: PsiBlock.Code, commenter: Commenter): ActionResult {
        TODO()
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
}

private class SelectionContext(val editor: Editor, val first: PsiElement, val last: PsiElement) {
    val selection: SelectionModel inline get() = editor.selectionModel
    val document: Document inline get() = editor.document

    val startLineIndex by lazy { document.getLineNumber(first.startOffset) }
    val endLineIndex by lazy { document.getLineNumber(last.endOffset) }

    val isInline: Boolean
        get() = last.nextNotEmptyLeaf !is PsiWhiteSpace

    val isMultiLine: Boolean
        get() = startLineIndex != endLineIndex

    fun insertAround(prefix: String?, suffix: String?, caret: Int): ActionResult {
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

private fun SelectionContext.wrapAroundLine(
    commenter: Commenter,
    @Language("Stitcher") opener: String? = null, @Language("Stitcher") closer: String? = null,
    openerCaret: Int = -1, closerCaret: Int = -1
): ActionResult {
    val (prefix, suffix) = commenter.getLineSurrounders()
        ?: return Err("stonecutter.action.wrap.err_no_commenter")
    val indent = document[document.getLineStartOffset(startLineIndex), first.startOffset]
    val opener = opener?.let { "$prefix$it$suffix\n$indent" }
    val closer = closer?.let { "\n$indent$prefix$it$suffix" }
    if (opener == null && closer == null) return Ok

    val caret = when {
        openerCaret >= 0 -> first.startOffset + prefix.length + openerCaret
        closerCaret >= 0 -> first.endOffset + indent.length + 1 + prefix.length + closerCaret
        else -> -1
    }
    return insertAround(opener, closer, caret)
}

private fun SelectionContext.wrapInLine(
    commenter: Commenter,
    @Language("Stitcher") opener: String? = null, @Language("Stitcher") closer: String? = null,
    openerCaret: Int = -1, closerCaret: Int = -1
): ActionResult {
    val (prefix, suffix) = commenter.getLineSurrounders()
        ?: return Err("stonecutter.action.wrap.err_no_commenter")
    val opener = opener?.let { "$prefix$it$suffix" }
    val closer = closer?.let { "$prefix$it$suffix" }
    if (opener == null && closer == null) return Ok

    val caret = when {
        openerCaret >= 0 -> first.startOffset + prefix.length + openerCaret
        closerCaret >= 0 -> first.endOffset + prefix.length + closerCaret
        else -> -1
    }
    return insertAround(opener, closer, caret)
}

private fun Commenter.getLineSurrounders(): Pair<String, String>? {
    lineCommentPrefix?.let { return it to "" }
    blockCommentPrefix?.let { pr -> blockCommentSuffix?.let { return pr to it } }
    return null
}

private fun PsiComment.findPrefixOffset(): Int {
    var position = innerRange.endOffset
    for (char in innerText.reversed()) when (char) {
        ' ', '\t' -> position -= 1
        else -> break
    }
    return startOffset + position
}

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Document.get(start: Int, end: Int): String =
    getText(TextRange(start, end))
