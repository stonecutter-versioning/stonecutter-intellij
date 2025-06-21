package dev.kikugie.stonecutter.intellij.util

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import dev.kikugie.stonecutter.intellij.lang.StitcherFile

internal val PsiComment.stitcherFile: StitcherFile?
    get() = InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(this)
        ?.firstNotNullOfOrNull { it.first as? StitcherFile }

internal val StitcherFile.containingComment: PsiComment
    get() = FileContextUtil.getFileContext(this) as PsiComment

internal val PsiElement.childrenSequence: Sequence<PsiElement> get() = sequence {
    var child: PsiElement? = firstChild
    while (child != null) {
        yield(child)
        child = child.nextSibling
    }
}

internal fun PsiElement.childrenSeqOfType(type: IElementType): Sequence<PsiElement> =
    childrenSequence.filter { it.elementType == type }

internal inline fun <reified T : PsiElement> PsiElement.childrenSeqOfType(): Sequence<T> =
    childrenSequence.filterIsInstance<T>()