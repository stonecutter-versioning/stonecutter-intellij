package dev.kikugie.stonecutter.intellij.editor.inspection.outer

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import dev.kikugie.stonecutter.intellij.lang.access.ScopeDefinition
import dev.kikugie.stonecutter.intellij.lang.access.ScopeType
import dev.kikugie.stonecutter.intellij.lang.util.commentDefinition
import java.lang.ref.WeakReference
import kotlin.collections.plusAssign
import kotlin.reflect.KClass

@Deprecated("Is hard")
class StitcherOuterScopeInspection : LocalInspectionTool(), DumbAware {
    override fun runForWholeFile(): Boolean = true
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
        Visitor(holder, session)

    override fun inspectionFinished(session: LocalInspectionToolSession, problemsHolder: ProblemsHolder) {
        val stack = session.getUserData(STACK_KEY) ?: return
        session.removeUserData(STACK_KEY) // TODO: report unclosed scopes
    }

    private class Visitor(val holder: ProblemsHolder, session: LocalInspectionToolSession) : PsiElementVisitor() {
        val stack: MutableList<ScopeEntry> = mutableListOf(ScopeEntry())

        init {
            session.putUserData(STACK_KEY, stack)
        }

        override fun visitElement(element: PsiElement) {
            super.visitElement(element)
            (element as? PsiComment)?.commentDefinition?.element?.let(::visitDefinition)
        }

        private fun visitDefinition(scope: ScopeDefinition) = when (scope.type) {
            ScopeType.OPENER -> stack += ScopeEntry(scope)
            ScopeType.EXTENSION -> visitExtension(scope)
            ScopeType.CLOSER -> {}
            ScopeType.INVALID -> {}
        }

        private fun visitExtension(scope: ScopeDefinition) {
            val (current, statements) = stack.last()
        }
    }

    private data class ScopeEntry(
        val type: KClass<out ScopeDefinition>?,
        val statements: MutableList<WeakReference<ScopeDefinition>>
    ) {
        constructor() : this(null, mutableListOf())
        constructor(start: ScopeDefinition) : this(start::class, mutableListOf(WeakReference(start)))

        operator fun plusAssign(statement: ScopeDefinition) {
            statements += WeakReference(statement)
        }
    }

    private companion object {
        val STACK_KEY: Key<List<ScopeEntry>> = Key("STITCHER_SCOPE_STACK")
    }
}