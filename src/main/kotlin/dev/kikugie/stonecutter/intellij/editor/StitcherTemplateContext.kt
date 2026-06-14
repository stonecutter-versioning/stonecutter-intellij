package dev.kikugie.stonecutter.intellij.editor

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiComment
import dev.kikugie.stonecutter.intellij.lang.StitcherFile

class StitcherTemplateContext : TemplateContextType("Stonecutter") {
    override fun isInContext(ctx: TemplateActionContext): Boolean =
        ctx.file !is StitcherFile && ctx.file.findElementAt(ctx.startOffset) is PsiComment
}