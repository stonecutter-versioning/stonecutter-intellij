package dev.kikugie.stonecutter.intellij.lang.util

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.nullableLazyValueUnsafe
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.util.descendantsOfType
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.access.OpenerType
import dev.kikugie.stonecutter.intellij.lang.access.ScopeDefinition

private val SCOPE_DEF: Key<SmartPsiElementPointer<ScopeDefinition>> = Key.create("STITCHER_DEFINITION")

internal val PsiComment.stitcherFile: StitcherFile?
    get() = InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(this)
        ?.firstNotNullOfOrNull { it.first as? StitcherFile }

internal val StitcherFile.containingComment: PsiComment
    get() = FileContextUtil.getFileContext(this) as PsiComment

internal val PsiComment.commentDefinition: SmartPsiElementPointer<ScopeDefinition>? get() = nullableLazyValueUnsafe(SCOPE_DEF) {
    val file = stitcherFile ?: return@nullableLazyValueUnsafe null
    val def = file.descendantsOfType<ScopeDefinition>().firstOrNull()
        ?: return@nullableLazyValueUnsafe null
    SmartPointerManager.getInstance(project).createSmartPsiElementPointer(def, file)
}

internal val PsiElement?.openerType: OpenerType get() = when(this?.text ?: "\u0000") {
    "{" -> OpenerType.OPEN
    ">>" -> OpenerType.WORD
    "\u0000" -> OpenerType.LINE
    else -> error("Element is not a scope opener")
}