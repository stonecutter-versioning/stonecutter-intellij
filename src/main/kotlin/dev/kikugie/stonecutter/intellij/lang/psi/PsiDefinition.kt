package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.elementType
import dev.kikugie.commons.collections.findIsInstance
import dev.kikugie.stonecutter.intellij.lang.impl.PsiStitcherNodeImpl
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherCompositeType.*
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.cached
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import dev.kikugie.stonecutter.intellij.lang.util.compositeType
import dev.kikugie.stonecutter.intellij.lang.util.elementOfAnyToken
import dev.kikugie.stonecutter.intellij.lang.util.elementOfToken
import dev.kikugie.stonecutter.intellij.lang.util.reverseChildrenSequence

private val SCOPE_OPENERS = arrayOf(LOOKUP_SCOPE.asIElementType(), CLOSED_SCOPE.asIElementType())
private val LITERALS = intArrayOf(StitcherLexer.IDENTIFIER, StitcherLexer.QUOTED)

sealed interface PsiDefinition : PsiStitcherNode {
    val kind: Kind

    val closer: PsiElement? get() = firstChild?.takeIf { it.antlrType == StitcherLexer.SCOPE_CLOSE }
    val opener: PsiElement? get() = lastChild?.takeIf { it.elementType in SCOPE_OPENERS }

    fun <T> accept(visitor: Visitor<T>): T
    override fun <T> accept(visitor: StitcherVisitor<T>): T = accept(visitor as Visitor<T>)

    interface Visitor<T> {
        fun visitSwapLocal(swap: PsiSwap.Local): T
        fun visitSwapOpener(swap: PsiSwap.Opener): T
        fun visitSwapCloser(swap: PsiSwap.Closer): T
        fun visitConditionOpener(condition: PsiCondition.Opener): T
        fun visitConditionExtension(condition: PsiCondition.Extension): T
        fun visitConditionCloser(condition: PsiCondition.Closer): T
        fun visitReplacementToggle(replacement: PsiReplacement.Toggle): T
        fun visitReplacementLocal(replacement: PsiReplacement.Local): T
        fun visitReplacementCloser(replacement: PsiReplacement.Closer): T
    }

    enum class Kind {
        SCOPED_OPENER, LINE_OPENER, LOOKUP_OPENER,
        SCOPED_EXTENSION, LINE_EXTENSION, LOOKUP_EXTENSION,
        CLOSER, INDEPENDENT;

        val isScoped: Boolean
            get() = when (this) {
                SCOPED_OPENER, SCOPED_EXTENSION -> true
                else -> false
            }

        val isOpen: Boolean
            get() = when (this) {
                LINE_OPENER, LOOKUP_OPENER, LINE_EXTENSION, LOOKUP_EXTENSION -> true
                else -> false
            }

        val isExtension: Boolean
            get() = when (this) {
                SCOPED_EXTENSION, LINE_EXTENSION, LOOKUP_EXTENSION, CLOSER -> true
                else -> false
            }

        val isEmpty: Boolean
            get() = when (this) {
                CLOSER, INDEPENDENT -> true
                else -> false
            }
    }

    companion object {
        val KIND_KEY: Key<CachedValue<Kind>> = Key("PsiDefinition.kind")
    }
}

sealed interface PsiSwap : PsiDefinition {
    class Local(node: ASTNode) : PsiStitcherNodeImpl(node), PsiSwap {
        override val kind: PsiDefinition.Kind by cached(PsiDefinition.KIND_KEY, opener::openerKind)
        override fun <T> accept(visitor: PsiDefinition.Visitor<T>): T = visitor.visitSwapLocal(this)
    }

    class Opener(node: ASTNode) : PsiStitcherNodeImpl(node), PsiSwap {
        val identifier: PsiElement? get() = firstChild
        val args: Sequence<PsiElement> get() = childrenSequence.filter { it != identifier && it.antlrType in LITERALS }

        override val kind: PsiDefinition.Kind by cached(PsiDefinition.KIND_KEY, opener::openerKind)
        override fun <T> accept(visitor: PsiDefinition.Visitor<T>): T = visitor.visitSwapOpener(this)
    }

    class Closer(node: ASTNode) : PsiStitcherNodeImpl(node), PsiSwap {
        override val kind: PsiDefinition.Kind get() = PsiDefinition.Kind.CLOSER
        override fun <T> accept(visitor: PsiDefinition.Visitor<T>): T = visitor.visitSwapCloser(this)
    }
}

sealed interface PsiCondition : PsiDefinition {
    class Opener(node: ASTNode) : PsiStitcherNodeImpl(node), PsiCondition {
        override val kind: PsiDefinition.Kind by cached(PsiDefinition.KIND_KEY, opener::openerKind)
        override fun <T> accept(visitor: PsiDefinition.Visitor<T>): T = visitor.visitConditionOpener(this)
    }

    class Extension(node: ASTNode) : PsiStitcherNodeImpl(node), PsiCondition {
        override val kind: PsiDefinition.Kind by cached(PsiDefinition.KIND_KEY, opener::extensionKind)
        override fun <T> accept(visitor: PsiDefinition.Visitor<T>): T = visitor.visitConditionExtension(this)
    }

    class Closer(node: ASTNode) : PsiStitcherNodeImpl(node), PsiCondition {
        override val kind: PsiDefinition.Kind get() = PsiDefinition.Kind.CLOSER
        override fun <T> accept(visitor: PsiDefinition.Visitor<T>): T = visitor.visitConditionCloser(this)
    }
}

sealed interface PsiReplacement : PsiDefinition {
    class Toggle(node: ASTNode) : PsiStitcherNodeImpl(node), PsiReplacement {
        val entries: Sequence<Entry> get() = childrenSequence.filterIsInstance<Entry>()
        override val kind: PsiDefinition.Kind get() = PsiDefinition.Kind.INDEPENDENT
        override fun <T> accept(visitor: PsiDefinition.Visitor<T>): T = visitor.visitReplacementToggle(this)
    }

    class Local(node: ASTNode) : PsiStitcherNodeImpl(node), PsiReplacement {
        val condition: PsiExpression? get() = childrenSequence.findIsInstance<PsiExpression>()
        val source: PsiElement? get() = childrenSequence.elementOfAnyToken(*LITERALS)
        val target: PsiElement? get() = reverseChildrenSequence.elementOfAnyToken(*LITERALS)

        override val kind: PsiDefinition.Kind by cached(PsiDefinition.KIND_KEY, opener::openerKind)
        override fun <T> accept(visitor: PsiDefinition.Visitor<T>): T = visitor.visitReplacementLocal(this)
    }

    class Closer(node: ASTNode) : PsiStitcherNodeImpl(node), PsiReplacement {
        override val kind: PsiDefinition.Kind get() = PsiDefinition.Kind.CLOSER
        override fun <T> accept(visitor: PsiDefinition.Visitor<T>): T = visitor.visitReplacementCloser(this)
    }

    class Entry(node: ASTNode) : PsiStitcherNodeImpl(node) {
        val op: PsiElement? get() = firstChild?.takeIf { it.antlrType == StitcherLexer.OP_NOT }
        val identifier: PsiElement? get() = lastChild
    }
}

private val PsiElement?.openerKind: PsiDefinition.Kind
    get() = when(compositeType) {
        CLOSED_SCOPE -> PsiDefinition.Kind.SCOPED_OPENER
        LOOKUP_SCOPE -> PsiDefinition.Kind.LOOKUP_OPENER
        else -> PsiDefinition.Kind.LINE_OPENER
    }

private val PsiElement?.extensionKind: PsiDefinition.Kind
    get() = when(compositeType) {
        CLOSED_SCOPE -> PsiDefinition.Kind.SCOPED_EXTENSION
        LOOKUP_SCOPE -> PsiDefinition.Kind.LOOKUP_EXTENSION
        else -> PsiDefinition.Kind.LINE_EXTENSION
    }
