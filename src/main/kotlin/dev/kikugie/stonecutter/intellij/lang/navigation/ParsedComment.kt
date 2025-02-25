package dev.kikugie.stonecutter.intellij.lang.navigation

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.getOrCreateUserData
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import dev.kikugie.stitcher.data.component.Definition
import dev.kikugie.stitcher.eval.eval
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.exception.join
import dev.kikugie.stitcher.parser.CommentParser
import dev.kikugie.stonecutter.AnyVersion
import dev.kikugie.stonecutter.data.parameters.BuildParameters
import dev.kikugie.stonecutter.intellij.lang.token.StitcherTypes
import dev.kikugie.stonecutter.intellij.util.string

class ParsedComment(element: PsiElement) {
    val definition by lazy { parseComment(element) }
    val valid get() = definition.isSuccess
    val type get() = definition.getOrNull()?.type

    fun evaluate(parameters: BuildParameters, version: AnyVersion): Result<Boolean> = definition.mapCatching {
        @Suppress("UnstableApiUsage")
        it.eval(parameters.toTransformParameters(version, "minecraft"))
    }

    companion object {
        private val KEY = Key<ParsedComment>("stonecutter.parsed_comment")
        fun get(element: PsiElement): ParsedComment? = when {
            element.elementType == StitcherTypes.Component.DEFINITION ->
                element.getOrCreateUserData(KEY) { ParsedComment(element) }

            element.parent != null -> get(element.parent)
            else -> null
        }

        private fun parseComment(element: PsiElement): Result<Definition> = kotlin.runCatching {
            require(element.elementType == StitcherTypes.Component.DEFINITION)
                { "Must start at component root" }

            val handler = StoringErrorHandler()
            val definition = requireNotNull(CommentParser.create(element.string, handler).parse())
                { "Parser has decided that the comment doesn't have any code" }
            if (handler.errors.isNotEmpty()) throw composeErrors(handler)
            definition
        }

        private fun composeErrors(handler: ErrorHandler): Throwable = RuntimeException("Failed to parse comment").apply {
            for (it in handler.errors) addSuppressed(RuntimeException(it.join()))
        }
    }
}