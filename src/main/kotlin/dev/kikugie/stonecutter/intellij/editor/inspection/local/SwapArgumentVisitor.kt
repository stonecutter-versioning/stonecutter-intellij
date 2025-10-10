package dev.kikugie.stonecutter.intellij.editor.inspection.local

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.util.lastLeaf
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherLocalInspectionTool
import dev.kikugie.stonecutter.intellij.editor.inspection.error
import dev.kikugie.stonecutter.intellij.lang.impl.SwapTemplate
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.service.stonecutterNode

private fun String.templates(): Sequence<IntRange> {
    val lexer = SwapTemplate(reader())
    return generateSequence { if (lexer.advance() != null) lexer.tokenStart..<lexer.tokenEnd else null }
}

class SwapArgumentVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : StitcherLocalInspectionTool.Visitor(holder, session) {
    override fun visitSwap(o: PsiSwap) {
        val identifier = o.identifier?.text ?: return
        val replacement = o.stonecutterNode?.params?.swaps?.get(identifier) ?: return
        val arguments = o.arguments?.entries.orEmpty().toList()
        val tokens = replacement.templates()
            .mapNotNull { replacement.substring(it).removePrefix("$").toIntOrNull() }
            .maxOrNull() ?: 0
        if (arguments.size < tokens) holder.error(o.lastLeaf(), "stonecutter.inspection.swap_args.too_few", tokens)
        else if (arguments.size > tokens) for (arg in arguments.drop(tokens))
            holder.error(arg, "stonecutter.inspection.swap_args.too_many")
    }
}