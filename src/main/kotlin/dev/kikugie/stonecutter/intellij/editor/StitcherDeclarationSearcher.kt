package dev.kikugie.stonecutter.intellij.editor

import com.intellij.pom.PomDeclarationSearcher
import com.intellij.pom.PomTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import com.intellij.util.Consumer
import dev.kikugie.commons.collections.findIsInstance
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtOperationExpression
import org.jetbrains.kotlin.psi.KtScript
import org.jetbrains.kotlin.psi.KtVisitor

private typealias Collector = Consumer<in PomTarget>

private val KtElement.operation: KtOperationExpression?
    get() = parents(false).findIsInstance<KtOperationExpression>()

class StitcherDeclarationSearcher : PomDeclarationSearcher() {
    override fun findDeclarationsAt(element: PsiElement, offsetInElement: Int, consumer: Collector) {
        if (element is KtElement) element.accept(KtDeclarationSearcher, consumer)
        println("${element::class.qualifiedName}: ${element.text.lineSequence().first()}")
    }

    // TODO: Navigate the file to find Stonecutter function invocations and record keys as declarations
    // Problem being that it's manual AF, and any syntax changes on the Stonecutter's side could brick it.
    private object KtDeclarationSearcher : KtVisitor<Unit, Collector>() {
        override fun visitScript(script: KtScript, data: Collector) {

        }

        override fun visitBlockExpression(expression: KtBlockExpression, data: Collector) {
            if (expression.operation?.kotlinFqName?.asString() != "Stonecutter_gradle") return
            // Iterate through statements
        }

        override fun visitBinaryExpression(expression: KtBinaryExpression, data: Collector) {

        }
    }
}