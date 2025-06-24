package dev.kikugie.stonecutter.intellij.editor

import com.intellij.codeInsight.CodeInsightUtil
import com.intellij.lang.surroundWith.SurroundDescriptor
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsActions
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

@Deprecated("Not finished yet")
class StitcherSurroundDescriptor : SurroundDescriptor {
    private val SURROUNDERS: Array<out Surrounder> = arrayOf(ConditionSurrounder)

    override fun isExclusive(): Boolean = false
    override fun getSurrounders(): Array<out Surrounder?> = SURROUNDERS
    override fun getElementsToSurround(file: PsiFile, startOffset: Int, endOffset: Int): Array<out PsiElement> =
        CodeInsightUtil.findStatementsInRange(file, startOffset, endOffset)

    private object ConditionSurrounder : Surrounder {
        override fun getTemplateDescription(): @NlsActions.ActionText String? = "? if"
        override fun isApplicable(elements: Array<out PsiElement?>): Boolean = true

        override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange? {
            return null // TODO
        }
    }
}