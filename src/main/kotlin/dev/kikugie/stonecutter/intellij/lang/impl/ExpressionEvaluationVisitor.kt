package dev.kikugie.stonecutter.intellij.lang.impl

import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.model.SCProjectNode

class ExpressionEvaluationVisitor(val node: SCProjectNode) : PsiExpression.Visitor<Boolean> {
    override fun visitGroup(group: PsiExpression.Group): Boolean = group.body.eval()

    override fun visitUnary(unary: PsiExpression.Unary): Boolean = !unary.target.eval()

    override fun visitBinary(binary: PsiExpression.Binary): Boolean = when (binary.operator.antlrType) {
        StitcherLexer.OP_OR -> binary.left.eval() || binary.right.eval()
        StitcherLexer.OP_AND -> binary.left.eval() && binary.right.eval()
        else -> false
    }

    override fun visitConstant(constant: PsiExpression.Constant): Boolean {
        return node.params.constants[constant.text] ?: false
    }

    override fun visitAssignment(assignment: PsiExpression.Assignment): Boolean {
        val version = node.params.dependencies[assignment.target?.text.orEmpty()] ?: return false
        return assignment.predicates.all { it.parsed?.invoke(version) ?: false }
    }

    private fun PsiExpression?.eval(): Boolean = this?.accept(this@ExpressionEvaluationVisitor) ?: false
}