package dev.kikugie.stonecutter.intellij.editor.imports

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiImportStatementBase
import dev.kikugie.stonecutter.intellij.lang.access.ScopeType

internal sealed interface CategorizedImport

internal data class RegularImport(val import: PsiImportStatementBase) : CategorizedImport

internal data class GroupedImport(val condition: PsiComment, val imports: List<PsiImportStatementBase>, val type: ScopeType) : CategorizedImport