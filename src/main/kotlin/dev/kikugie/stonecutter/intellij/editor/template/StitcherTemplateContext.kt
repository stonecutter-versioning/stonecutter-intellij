package dev.kikugie.stonecutter.intellij.editor.template

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import dev.kikugie.stonecutter.intellij.lang.StitcherFile

class StitcherTemplateContext : TemplateContextType("Stonecutter") {
    override fun isInContext(ctx: TemplateActionContext): Boolean =
        ctx.file is StitcherFile
}
