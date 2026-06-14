package dev.kikugie.stonecutter.intellij.debug

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import java.awt.event.HierarchyEvent
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import dev.kikugie.commons.takeAsOrNull
import dev.kikugie.stonecutter.intellij.lang.psi.PsiBlock
import dev.kikugie.stonecutter.intellij.lang.psi.getStitcherAst
import dev.kikugie.stonecutter.intellij.lang.util.innerText
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class PsiBlockNavigatorPanel(private val project: Project, toolWindow: ToolWindow) : JPanel(BorderLayout()) {
    private val parentDisposable: Disposable = toolWindow.disposable
    private val tree: Tree = Tree()
    private var currentEditor: Editor? = null
    private var currentHighlighter: RangeHighlighter? = null
    private var editorDisposable: Disposable? = null
    private var caretSyncEnabled: Boolean = true
    private var suppressSync: Boolean = false

    init {
        tree.isRootVisible = true
        tree.showsRootHandles = true
        tree.cellRenderer = BlockNodeRenderer()
        tree.model = DefaultTreeModel(DefaultMutableTreeNode("No file selected"))
        tree.addTreeSelectionListener { onTreeSelectionChanged() }

        add(buildToolbar(), BorderLayout.NORTH)
        add(JBScrollPane(tree), BorderLayout.CENTER)

        project.messageBus.connect(parentDisposable).subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            createEditorListener(),
        )

        attachEditor(FileEditorManager.getInstance(project).selectedTextEditor)

        addHierarchyListener { event ->
            if (event.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L && !isShowing)
                removeHighlight()
        }

        Disposer.register(parentDisposable) { removeHighlight() }
    }

    private fun createEditorListener(): FileEditorManagerListener = object : FileEditorManagerListener {
        override fun selectionChanged(event: FileEditorManagerEvent) {
            attachEditor((event.newEditor as? TextEditor)?.editor)
        }
    }

    private fun buildToolbar(): Component {
        val group = DefaultActionGroup().apply {
            add(createRefreshAction())
            addSeparator()
            add(createCaretSyncAction())
        }
        val action = ActionManager.getInstance().createActionToolbar("PsiBlockNavigator", group, true).apply {
            targetComponent = this@PsiBlockNavigatorPanel
        }
        return action.component
    }

    private fun createRefreshAction(): AnAction = object : AnAction(
        "Refresh", "Rebuild the PsiBlock tree for the current file", AllIcons.Actions.Refresh
    ) {
        override fun getActionUpdateThread() = ActionUpdateThread.EDT
        override fun actionPerformed(e: AnActionEvent) {
            currentEditor?.let { rebuildTree(it) }
        }
    }

    private fun createCaretSyncAction(): ToggleAction = object : ToggleAction(
        "Sync with Caret", "Auto-select the tree node containing the editor caret", AllIcons.General.AutoscrollFromSource
    ) {
        override fun getActionUpdateThread() = ActionUpdateThread.EDT
        override fun isSelected(e: AnActionEvent) = caretSyncEnabled
        override fun setSelected(e: AnActionEvent, state: Boolean) {
            caretSyncEnabled = state
        }
    }

    private fun attachEditor(editor: Editor?) {
        editorDisposable?.let { Disposer.dispose(it) }
        editorDisposable = null
        removeHighlight()
        currentEditor = editor

        if (editor == null) {
            tree.model = DefaultTreeModel(DefaultMutableTreeNode("No file selected"))
            return
        }

        val d = Disposer.newDisposable(parentDisposable)
        editorDisposable = d

        editor.caretModel.addCaretListener(createCaretListener(), d)
        rebuildTree(editor)
    }

    private fun createCaretListener(): CaretListener = object : CaretListener {
        override fun caretPositionChanged(event: CaretEvent) {
            if (!isShowing) return
            if (caretSyncEnabled && !suppressSync) syncTreeFromCaret(event.editor)
        }
    }

    private fun rebuildTree(editor: Editor) {
        val vFile = editor.virtualFile ?: run {
            tree.model = DefaultTreeModel(DefaultMutableTreeNode("No virtual file"))
            return
        }
        ApplicationManager.getApplication().runReadAction {
            val psiFile = PsiManager.getInstance(project).findFile(vFile)
            val newModel =
                if (psiFile == null) DefaultTreeModel(DefaultMutableTreeNode("No PSI file"))
                else DefaultTreeModel(psiFile.getStitcherAst().accept(TreeNodeCollector))
            SwingUtilities.invokeLater {
                tree.model = newModel
                expandAll()
            }
        }
    }

    private fun expandAll() {
        var i = 0
        while (i < tree.rowCount) tree.expandRow(i++)
    }

    private fun onTreeSelectionChanged() {
        val data = (tree.lastSelectedPathComponent as? DefaultMutableTreeNode)
            ?.userObject as? BlockData ?: return
        val editor = currentEditor ?: return

        removeHighlight()
        val range = data.range

        suppressSync = true
        try {
            editor.scrollingModel.scrollTo(
                editor.offsetToLogicalPosition(range.startOffset),
                ScrollType.MAKE_VISIBLE,
            )
            currentHighlighter = editor.markupModel.addRangeHighlighter(
                range.startOffset.coerceAtLeast(0),
                range.endOffset.coerceAtMost(editor.document.textLength),
                HighlighterLayer.SELECTION - 1,
                TextAttributes().apply { backgroundColor = data.highlightColor },
                HighlighterTargetArea.EXACT_RANGE,
            )
        } catch (_: Exception) {
        } finally {
            suppressSync = false
        }
    }

    private fun removeHighlight() {
        currentHighlighter?.let { currentEditor?.markupModel?.removeHighlighter(it) }
        currentHighlighter = null
    }

    private fun syncTreeFromCaret(editor: Editor) {
        val offset = editor.caretModel.offset
        val root = tree.model?.root as? DefaultMutableTreeNode ?: return
        val node = findDeepestAt(root, offset) ?: return

        suppressSync = true
        removeHighlight()
        try {
            val path = TreePath(node.path)
            tree.selectionPath = path
            tree.scrollPathToVisible(path)
        } finally {
            suppressSync = false
        }
    }

    private fun findDeepestAt(node: DefaultMutableTreeNode, offset: Int): DefaultMutableTreeNode? {
        val data = node.userObject as? BlockData ?: return null
        if (!data.range.contains(offset)) return null
        for (i in 0 until node.childCount) {
            val child = node.getChildAt(i) as? DefaultMutableTreeNode ?: continue
            findDeepestAt(child, offset)?.let { return it }
        }
        return node
    }

    private data class BlockData(val block: PsiBlock) {
        val range: TextRange = block.accept(TextRangeMapper)
        val label: String = block.label()

        val textColor: Color inline get() = block.accept(TextColorMapper)
        val highlightColor: Color inline get() = block.accept(HighlightColorMapper)

        private fun PsiElement.label(): String =
            "${this::class.simpleName} $startOffset..<$endOffset${block.accept(TextSampleExtractor)}"
    }

    private class BlockNodeRenderer : DefaultTreeCellRenderer() {
        override fun getTreeCellRendererComponent(
            tree: JTree, value: Any, selected: Boolean,
            expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean,
        ): Component {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
            icon = null
            val data = value.takeAsOrNull<DefaultMutableTreeNode>()?.userObject as? BlockData
                ?: return this
            text = data.label

            if (!selected) foreground = data.textColor
            return this
        }
    }

    private object TreeNodeCollector : PsiBlock.Visitor<DefaultMutableTreeNode> {
        override fun visitContent(content: PsiBlock.Content): DefaultMutableTreeNode =
            DefaultMutableTreeNode(BlockData(content))

        override fun visitComment(comment: PsiBlock.Comment): DefaultMutableTreeNode =
            DefaultMutableTreeNode(BlockData(comment))

        override fun visitCode(code: PsiBlock.Code): DefaultMutableTreeNode {
            val node = DefaultMutableTreeNode(BlockData(code))
            for (it in code.entries) node.add(it.accept(this))
            return node
        }

        override fun visitRoot(root: PsiBlock.Root): DefaultMutableTreeNode {
            val node = DefaultMutableTreeNode(BlockData(root))
            for (it in root.entries) node.add(it.accept(this))
            return node
        }
    }

    private object TextSampleExtractor : PsiBlock.Visitor<String> {
        override fun visitContent(content: PsiBlock.Content): String = " — '${content.text.trimWithEllipsis(20)}'"
        override fun visitComment(comment: PsiBlock.Comment): String = " — '${comment.hostComment?.element?.innerText?.trimWithEllipsis(20) ?: "?"}'"
        override fun visitCode(code: PsiBlock.Code): String = " — '${code.hostComment?.element?.innerText?.trimWithEllipsis(20) ?: "?"}'"
        override fun visitRoot(root: PsiBlock.Root): String = ""

        private fun String.trimWithEllipsis(length: Int, ellipsis: String = "..."): String {
            val formatted = replace("\n", "\\n")
            return if (formatted.length + ellipsis.length <= length) formatted
            else formatted.take(length - ellipsis.length) + ellipsis
        }
    }

    private object TextRangeMapper : PsiBlock.Visitor<TextRange> {
        override fun visitContent(content: PsiBlock.Content): TextRange = content.textRange
        override fun visitComment(comment: PsiBlock.Comment): TextRange = comment.textRange
        override fun visitRoot(root: PsiBlock.Root): TextRange = root.textRange
        override fun visitCode(code: PsiBlock.Code): TextRange =
            TextRange(code.startOffset, code.lastChild?.endOffset ?: code.endOffset)
    }

    private object TextColorMapper : PsiBlock.Visitor<Color> {
        override fun visitContent(content: PsiBlock.Content): Color = JBColor(Color(0, 110, 0), Color(70, 175, 70))
        override fun visitComment(comment: PsiBlock.Comment): Color = JBColor(Color(160, 80, 0), Color(200, 150, 70))
        override fun visitCode(code: PsiBlock.Code): Color = JBColor(Color(0, 80, 180), Color(90, 150, 240))
        override fun visitRoot(root: PsiBlock.Root): Color = JBColor.GRAY
    }

    private object HighlightColorMapper : PsiBlock.Visitor<Color> {
        override fun visitContent(content: PsiBlock.Content): Color = JBColor(Color(0, 150, 0, 50), Color(30, 150, 50, 60))
        override fun visitComment(comment: PsiBlock.Comment): Color = JBColor(Color(210, 130, 0, 50), Color(180, 130, 50, 60))
        override fun visitCode(code: PsiBlock.Code): Color = JBColor(Color(0, 100, 220, 50), Color(40, 100, 220, 60))
        override fun visitRoot(root: PsiBlock.Root): Color = JBColor(Color(100, 100, 200, 50), Color(80, 80, 180, 60))
    }
}
