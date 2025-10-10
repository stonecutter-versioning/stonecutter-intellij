package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.ImportOptimizer
import com.intellij.psi.PsiFile
import kotlinx.coroutines.Runnable

// FIXME: temporarily disabled
class StitcherImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean = false
        // file is PsiJavaFile && StonecutterSettings.STATE.useImportOptimizer

    override fun processFile(file: PsiFile): Runnable {
        return Runnable {  }
        /*
        val imports: PsiImportList = (file as? PsiJavaFile)?.importList
            ?: return EmptyRunnable.getInstance()

        val optimized = JavaCodeStyleManager.getInstance(file.project).prepareOptimizeImportsResult(file)
            ?: return EmptyRunnable.getInstance()

        val groups = imports.buildImportGroups()
        if (groups.all { it is RegularImport })
            return file.getNativeOptimiser().processFile(file)

        return Runnable { groups.replaceImports(file, optimized) }

         */
    }

    /*
    private fun List<CategorizedImport>.replaceImports(file: PsiJavaFile, optimized: PsiImportList) {
        val range = textRange()
        val block = rearrangeImports(optimized).removeSuffix("\n")
        file.fileDocument.replaceString(range.startOffset, range.endOffset, block)
    }

    private fun List<CategorizedImport>.textRange(): TextRange {
        val start = when (val it = first()) {
            is RegularImport -> it.import.startOffset
            is GroupedImport -> it.condition.startOffset
        }
        val end = when(val it = last()) {
            is RegularImport -> it.import.endOffset
            is GroupedImport -> (it.imports.lastOrNull() ?: it.condition).endOffset
        }
        return TextRange(start, end)
    }

    private fun List<CategorizedImport>.rearrangeImports(optimized: PsiImportList) = buildString {
        val regular: MutableSet<PsiImportStatementBase> = optimized.allImportStatements.toMutableSet()
        val grouped: List<GroupedImport> = this@rearrangeImports.filterIsInstance<GroupedImport>()
        for (group in grouped) for (it in group.imports) regular -= it

        regular.joinTo(this, "\n") { it.text }
        appendLine()

        for (group in grouped) {
            appendLine(group.condition.text)
            for (it in group.imports)
                appendLine(it.text)
        }
    }

    private fun PsiImportList.buildImportGroups(): List<CategorizedImport> = buildList {
        val start: PsiElement = prevSiblings.findDefinition { it.type == ScopeType.OPENER } ?: this@buildImportGroups

        var condition: PsiComment? = null
        var inline = false
        var scope: ScopeType = ScopeType.INVALID
        val group: MutableList<PsiImportStatementBase> = mutableListOf()

        fun submit() {
            this += GroupedImport(condition!!, group.toList(), scope)
            condition = null; inline = false; scope = ScopeType.INVALID; group.clear()
        }

        for (item in start.importScope()) when (item) {
            is PsiImportStatementBase ->
                if (condition == null) this += RegularImport(item)
                else { group += item; if (inline) submit() }
            is PsiComment -> when(val def = item.commentDefinition?.element) {
                null -> if (condition != null) submit()
                else -> {
                    if (condition != null) submit()
                    condition = item

                    scope = def.type
                    if (scope == ScopeType.CLOSER) submit()
                    else if (scope != ScopeType.INVALID && def.opener.openerType != OpenerType.OPEN)
                        inline = true
                }
            }
        }
    }

    private fun PsiElement.importScope(): Sequence<PsiElement> = sequence {
        var nextIsLast = false
        for (it in siblings()) when (it) {
            is PsiImportList -> yieldAll(it.childrenSequence.filterNotWhitespace())
            is PsiComment -> {
                yield(it)
                if (nextIsLast) break
                val definition = it.commentDefinition?.element
                    ?: continue

                val type = definition.type
                if (type == ScopeType.CLOSER) break

                if (type == ScopeType.EXTENSION && definition.opener.openerType != OpenerType.OPEN)
                    nextIsLast = true
            }
        }
    }

    private fun PsiFile.getNativeOptimiser() = LanguageImportStatements.INSTANCE
        .allForLanguage(language).first { it != null && it != this@StitcherImportOptimizer && it.supports(this) }

    private inline fun Sequence<PsiElement>.findDefinition(check: (ScopeDefinition) -> Boolean): PsiElement? = filterNotWhitespace()
        .find { (it as? PsiComment)?.commentDefinition?.element?.let(check) ?: false }

     */
}