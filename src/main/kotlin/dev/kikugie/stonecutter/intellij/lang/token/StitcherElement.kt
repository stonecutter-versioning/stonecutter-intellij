package dev.kikugie.stonecutter.intellij.lang.token

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.tree.util.children
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import dev.kikugie.semver.Version
import dev.kikugie.stonecutter.data.parameters.BuildParameters
import dev.kikugie.stonecutter.intellij.impl.service.StonecutterModelLookup.Companion.modelLookup
import dev.kikugie.stonecutter.intellij.util.mapResult
import dev.kikugie.stonecutter.intellij.util.requireNotNullResult
import dev.kikugie.stonecutter.intellij.util.takeAs
import dev.kikugie.stonecutter.intellij.util.toStringList
import org.apache.commons.text.similarity.LevenshteinDistance
import org.jetbrains.plugins.groovy.util.TokenSet
import kotlin.sequences.filter

sealed class StitcherElement(node: ASTNode) : ASTWrapperPsiElement(node) {
    override fun isWritable(): Boolean = false

    companion object {
        val Reference<*>.present get() = this.keys.getOrNull()?.contains(this.node.text)
        val Reference<*>.type get() = this::class.simpleName?.lowercase() ?: "object"
        fun Reference<*>.findSimilar(): Sequence<String>  {
            val text = node.text
            return keys.getOrElse { emptyList() }.asSequence().filter {
                LevenshteinDistance.getDefaultInstance().apply(text, it) <= 2
            }
        }

        fun map(node: ASTNode) = when(node.elementType) {
            StitcherType.Reference.SWAP -> Reference.Swap(node)
            StitcherType.Reference.CONSTANT -> Reference.Constant(node)
            StitcherType.Reference.DEPENDENCY -> Reference.Dependency(node)
            StitcherType.Reference.AMBIGUOUS -> Reference.Ambiguous(node)

            StitcherType.Component.ASSIGNMENT -> Operation.Assignment(node)
            StitcherType.Component.BINARY -> Operation.Binary(node)
            StitcherType.Component.UNARY -> Operation.Unary(node)

            StitcherType.Component.SWAP -> Composite.Swap(node)
            StitcherType.Component.GROUP -> Composite.Group(node)
            StitcherType.Component.SUGAR -> Composite.Sugar(node)
            StitcherType.Component.PREDICATE -> Composite.Predicate(node)
            StitcherType.Component.CONDITION -> Composite.Condition(node)
            StitcherType.Component.EXPRESSION -> Composite.Expression(node)
            StitcherType.Component.DEFINITION -> Composite.Definition(node)
            else -> ASTWrapperPsiElement(node)
        }

        private fun ASTNode.reverseChildren() = generateSequence(lastChildNode) { it.treePrev }
        private inline fun < reified T : PsiElement> Sequence<ASTNode>.findType(condition: (StitcherType) -> Boolean) = find {
            val type = it.elementType as? StitcherType ?: return@find false
            it.psi is T && condition(type)
        }?.psi?.takeAs<T>()

        private inline fun <reified T : PsiElement> StitcherElement.first() = lazy {
            checkNotNull(findChildByClass(T::class.java)) { "Couldn't find element of type ${T::class.qualifiedName ?: "object"}" }
        }
        private inline fun <reified T : PsiElement> StitcherElement.first(type: StitcherType) = lazy {
            checkNotNull(findChildByType(type) as? T) { "Couldn't find element of type $type & ${T::class.qualifiedName ?: "object"}" }
        }
        private inline fun <reified T : PsiElement> StitcherElement.first(vararg types: StitcherType) = lazy {
            checkNotNull(findChildByType(TokenSet(*types)) as? T) { "Couldn't find element of type ${types.asIterable().toStringList()} & ${T::class.qualifiedName ?: "object"}" }
        }
        private inline fun <reified T : PsiElement> StitcherElement.first(crossinline condition: (StitcherType) -> Boolean) = lazy {
            val match = node.children().findType<T>(condition)
            checkNotNull(match) { "Couldn't find element of type ${T::class.qualifiedName ?: "object"} with the provided condition" }
        }

        private inline fun <reified T : PsiElement> StitcherElement.last() = lazy {
            val match = node.reverseChildren().find { it.psi is T }?.psi as? T
            checkNotNull(match) { "Couldn't find element of type ${T::class.qualifiedName ?: "object"}" }
        }
        private inline fun <reified T : PsiElement> StitcherElement.last(type: StitcherType) = lazy {
            val match = node.reverseChildren().findType<T> { it == type }
            checkNotNull(match) { "Couldn't find element of type $type & ${T::class.qualifiedName ?: "object"}" }
        }
        private inline fun <reified T : PsiElement> StitcherElement.last(vararg types: StitcherType) = lazy {
            val match = node.reverseChildren().findType<T> { it in types }
            checkNotNull(match) { "Couldn't find element of type ${types.asIterable().toStringList()} & ${T::class.qualifiedName ?: "object"}" }
        }
        private inline fun <reified T : PsiElement> StitcherElement.last(crossinline condition: (StitcherType) -> Boolean) = lazy {
            val match = node.reverseChildren().findType<T>(condition)
            checkNotNull(match) { "Couldn't find element of type ${T::class.qualifiedName ?: "object"} with the provided condition" }
        }
    }

    sealed interface Composite : PsiElement {
        class Predicate(node: ASTNode): StitcherElement(node), Composite {
            val entries: List<PsiElement> by lazy {
                node.children()
                    .filter { it.elementType == StitcherType.Primitive.PREDICATE }
                    .map { it.psi }
                    .toList()
            }
        }

        class Group(node: ASTNode): StitcherElement(node), Composite {
            val expression: PsiElement get() = children.first {
                it != StitcherType.Operator.LPAREN && it !is PsiWhiteSpace
            }
        }

        class Sugar(node: ASTNode): StitcherElement(node), Composite {
            val entries: List<PsiElement> by lazy {
                node.children()
                    .filter { it.elementType is StitcherType.Sugar }
                    .map { it.psi }
                    .toList()
            }
        }

        class Expression(node: ASTNode): StitcherElement(node), Composite {
            val value: StitcherElement by first<StitcherElement>()
        }

        class Condition(node: ASTNode): StitcherElement(node), Composite {
            val sugar: Sugar by first<Sugar>()
            val expression: Expression by last<Expression>()
        }

        class Swap(node: ASTNode): StitcherElement(node), Composite {
            val identifier: Reference.Swap by first<Reference.Swap>()
        }

        class Definition(node: ASTNode): StitcherElement(node), Composite {

        }
    }

    sealed interface Operation : PsiElement {
        class Unary(node: ASTNode): StitcherElement(node), Operation {
            val operator: PsiElement by first<PsiElement> { it is StitcherType.Operator }
            val target: PsiElement by last<StitcherElement>()
        }

        class Binary(node: ASTNode): StitcherElement(node), Operation {
            val operator: PsiElement by first<PsiElement> { it is StitcherType.Operator }
            val left: PsiElement by first<StitcherElement>()
            val right: PsiElement by last<StitcherElement>()
        }

        class Assignment(node: ASTNode): StitcherElement(node), Operation {
            val dependency: Reference.Dependency by first<Reference.Dependency>()
            val predicate: Composite.Predicate by last<Composite.Predicate>()
        }
    }

    sealed interface Reference<T> : PsiElement {
        val state: Result<T>
        val keys: Result<Collection<String>>

        fun buildParameters(): Result<BuildParameters> =
            modelLookup.activeVersionModel.map { it.parameters }

        class Swap(node: ASTNode) : ASTWrapperPsiElement(node), Reference<String> {
            override val keys: Result<Collection<String>> get() = buildParameters().map { it.swaps.keys }
            override val state: Result<String> get() = buildParameters().mapResult {
                requireNotNullResult(it.swaps[node.text]) { "Invalid swap reference" }
            }
        }

        class Constant(node: ASTNode) : ASTWrapperPsiElement(node), Reference<Boolean> {
            override val keys: Result<Collection<String>> get() = buildParameters().map { it.constants.keys }
            override val state: Result<Boolean> get() = buildParameters().mapResult {
                requireNotNullResult(it.constants[node.text]) { "Invalid constant reference" }
            }
        }

        class Dependency(node: ASTNode) : ASTWrapperPsiElement(node), Reference<Version> {
            override val keys: Result<Collection<String>> get() = buildParameters().map { it.dependencies.keys }
            override val state: Result<Version> get() = buildParameters().mapResult {
                requireNotNullResult(it.dependencies[node.text]) { "Invalid dependency reference" }
            }
        }

        class Ambiguous(node: ASTNode) : ASTWrapperPsiElement(node), Reference<String> {
            val dependencies: Result<Collection<String>> get() = buildParameters().map { it.dependencies.keys }
            val constants: Result<Collection<String>> get() = buildParameters().map { it.constants.keys }

            override val keys: Result<Collection<String>> get() = buildParameters().map {
                it.constants.keys + it.dependencies.keys
            }
            override val state: Result<String> get() = buildParameters().mapResult {
                val state = node.text.let { tx -> it.constants[tx] ?: it.dependencies[tx] }?.toString()
                requireNotNullResult(state) { "Invalid ambiguous reference" }
            }
        }
    }
}

