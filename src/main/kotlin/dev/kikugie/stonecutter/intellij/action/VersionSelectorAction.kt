package dev.kikugie.stonecutter.intellij.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopupStep
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.PopupUtil
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.JBColor
import com.intellij.ui.popup.WizardPopup
import com.intellij.ui.popup.list.ListPopupImpl
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import dev.kikugie.stonecutter.intellij.StonecutterIcons
import dev.kikugie.stonecutter.intellij.service.model.SCProjectNode
import dev.kikugie.stonecutter.intellij.service.model.SCProjectTree
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import dev.kikugie.stonecutter.intellij.util.GradleUtil
import java.awt.*
import java.awt.event.*
import java.nio.file.Path
import javax.swing.*

class VersionSelectorAction : ComboBoxAction(), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val trees = event.project?.stonecutterService?.lookup?.trees.orEmpty()
        buildActivePresentation(event.presentation, trees.values)
    }

    override fun createActionPopup(context: DataContext, component: JComponent, callback: Runnable?): JBPopup {
        val project = context.getData(CommonDataKeys.PROJECT) ?: throw UnsupportedOperationException("No project loaded")
        val trees = project.stonecutterService.lookup.trees.values.filter { it.current != null }
        return createVersionTable(trees, context, callback)
    }

    private fun buildActivePresentation(
        presentation: Presentation, trees: Collection<SCProjectTree>
    ): Unit = when (trees.size) {
        0 -> presentation.isEnabledAndVisible = false
        1 -> when (val current = trees.first().current) {
            null -> presentation.isEnabledAndVisible = false
            else -> {
                presentation.isEnabledAndVisible = true
                presentation.text = current
            }
        }
        else -> {
            presentation.text = trees.asSequence().filter { it.current != null }.joinToString(" | ") {
                "${it.project.identityPath} > ${it.current}"
            }
            presentation.isEnabledAndVisible = presentation.text.isNotBlank()
        }
    }

    private fun createVersionTable(trees: List<SCProjectTree>, context: DataContext, callback: Runnable?): JBPopup {
        if (trees.isEmpty()) return JBPopupFactory.getInstance().createMessage("No available versions")
        val step = TableStep(trees, context, callback, "Select Version")
        return TableGroupPopup(context.getData(CommonDataKeys.PROJECT), step)
    }
}

private class VersionSwitchAction(title: String, icon: Icon, val task: String, val path: Path) : AnAction(title, null, icon) {
    constructor(node: SCProjectNode) : this(
        node.metadata.project, node.icon(), "stonecutterSwitchTo${node.metadata.project}", node.tree.project.projectDir.toPath()
    )

    override fun actionPerformed(event: AnActionEvent) = when(val project = event.project) {
        null -> Messages.showMessageDialog("Couldn't access the project", "Error", null)
        else -> GradleUtil.runGradleTask(project, path) { taskNames = listOf(task) }
    }
}

private fun SCProjectNode.icon(): Icon = when (metadata.project) {
    tree.vcs -> StonecutterIcons.VERSION_VCS
    tree.current -> StonecutterIcons.VERSION_ENTRY
    else -> StonecutterIcons.VERSION_EMPTY
}

private class TableGroupPopup(private val project: Project?, table: TableStep) : WizardPopup(project, null, table) {
    private lateinit var columns: MutableList<TableColumn>
    private val table: TableStep inline get() = step as TableStep

    override fun createContent(): JComponent = JPanel(GridLayout(1, table.trees.size)).apply {
        columns = mutableListOf()
        PopupUtil.applyNewUIBackground(this)

        for (tree in table.trees) createColumn(tree).let {
            columns.add(it)
            add(createColumnPanel(tree, it))
        }
    }

    private fun createColumn(tree: SCProjectTree): TableColumn {
        val group = DefaultActionGroup().apply { addAll(createActions(tree)) }
        val dummy = JBPopupFactory.getInstance().createActionGroupPopup(
            null, group, table.context, JBPopupFactory.ActionSelectionAid.MNEMONICS, true
        )
        val action = dummy.listStep as ListPopupStep<Any>
        dummy.dispose()

        return TableColumn(project, this, action).apply {
            beforeShow()
            list.border = JBUI.Borders.empty()
            list.addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent) {
                    columns.filter { it != this@apply }.forEach { it.list.clearSelection() }
                }
            })
        }
    }

    private fun createActions(tree: SCProjectTree): List<VersionSwitchAction> = tree.nodes
        .distinctBy { it.metadata.project }
        .map(::VersionSwitchAction)
        .toList()

    private fun createColumnPanel(tree: SCProjectTree, column: TableColumn): JPanel = JPanel(BorderLayout()).apply {
        isOpaque = false
        add(createHeader(tree), BorderLayout.NORTH)
        add(column.list, BorderLayout.CENTER)
    }

    private fun createHeader(tree: SCProjectTree): JLabel = JLabel(tree.project.identityPath, SwingConstants.CENTER).apply {
        font = JBFont.small().deriveFont(Font.BOLD)
        foreground = JBColor.GRAY
        border = JBUI.Borders.empty(4, 8)
    }

    fun moveFocus(source: TableColumn, direction: Int) {
        val index = columns.indexOf(source)
        val nextIndex = index + direction
        if (nextIndex in columns.indices) {
            val nextColumn = columns[nextIndex]
            nextColumn.list.requestFocus()
            if (nextColumn.list.model.size > 0) {
                nextColumn.list.selectedIndex = source.list.selectedIndex.coerceIn(0, nextColumn.list.model.size - 1)
            }
            source.list.clearSelection()
        }
    }

    override fun getPreferredFocusableComponent(): JComponent? = columns.firstOrNull()?.list
    override fun getInputMap(): InputMap = columns.firstOrNull()?.list?.inputMap ?: InputMap()
    override fun getActionMap(): ActionMap = columns.firstOrNull()?.list?.actionMap ?: ActionMap()
    override fun onChildSelectedFor(value: Any?) {}

    override fun dispose() {
        (step as? TableStep)?.callback?.run()
        if (::columns.isInitialized) {
            columns.forEach { it.dispose() }
        }
        super.dispose()
    }
}

private class TableColumn(
    project: Project?,
    parent: WizardPopup,
    step: ListPopupStep<Any>
) : ListPopupImpl(project, parent, step, null) {
    public override fun beforeShow(): Boolean = super.beforeShow()

    override fun handleRightKeyPressed(keyEvent: KeyEvent) {
        (parent as? TableGroupPopup)?.moveFocus(this, 1) ?: super.handleRightKeyPressed(keyEvent)
    }

    override fun handleLeftKeyPressed(keyEvent: KeyEvent) {
        (parent as? TableGroupPopup)?.moveFocus(this, -1) ?: super.handleLeftKeyPressed(keyEvent)
    }
}

private class TableStep(
    val trees: List<SCProjectTree>,
    val context: DataContext,
    val callback: Runnable?,
    private val title: @NlsContexts.PopupTitle String
) : PopupStep<Any> {
    override fun getTitle(): String = title
    override fun onChosen(selectedValue: Any?, finalChoice: Boolean): PopupStep<*>? = null
    override fun hasSubstep(selectedValue: Any?): Boolean = false
    override fun canceled() {}
    override fun isMnemonicsNavigationEnabled(): Boolean = false
    override fun getMnemonicNavigationFilter() = null
    override fun isSpeedSearchEnabled(): Boolean = false
    override fun getSpeedSearchFilter() = null
    override fun isAutoSelectionEnabled(): Boolean = false
    override fun getFinalRunnable(): Runnable? = null
}
