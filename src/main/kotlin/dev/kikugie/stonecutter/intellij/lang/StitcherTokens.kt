package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.data.token.*
import dev.kikugie.stonecutter.intellij.impl.StitcherReference
import dev.kikugie.stonecutter.intellij.impl.StitcherReferenceImpl
import dev.kikugie.stonecutter.intellij.impl.StitcherReferenceType

class StitcherType(name: String) : IElementType(name, StitcherLang) {
    companion object {
        val CONDITION_MARKER = StitcherType("CONDITION_MARKER")
        val SWAP_MARKER = StitcherType("SWAP_MARKER")
        val IF = StitcherType("IF_KEYWORD")
        val ELSE = StitcherType("ELSE_KEYWORD")
        val ELIF = StitcherType("ELIF_KEYWORD")
        val NEGATE = StitcherType("NEGATION_OPERATOR")
        val AND = StitcherType("AND_OPERATOR")
        val OR = StitcherType("OR_OPERATOR")
        val GROUP_OPEN = StitcherType("LEFT_BRACE")
        val GROUP_CLOSE = StitcherType("RIGHT_BRACE")
        val EXPECT_WORD = StitcherType("EXPECT_WORD_MARKER")
        val SCOPE_OPEN = StitcherType("LEFT_BRACKET")
        val SCOPE_CLOSE = StitcherType("LEFT_BRACKET")
        val IDENTIFIER = StitcherType("IDENTIFIER")
        val ASSIGNMENT = StitcherType("ASSIGNMENT_OPERATOR")
        val PREDICATE = StitcherType("PREDICATE")

        // Inconvertible
        val STITCHER_EXPRESSION = StitcherType("STITCHER_EXPRESSION")
        val SWAP = StitcherType("STITCHER_SWAP")
        val CONDITION = StitcherType("STITCHER_CONDITION")

        val GROUP = StitcherType("STITCHER_GROUP")

        fun create(type: StitcherReferenceType, node: ASTNode): StitcherReference =
            StitcherReferenceImpl(type, node)
    }
}

fun TokenType.convert(): IElementType = when (this) {
    WhitespaceType -> WHITE_SPACE
    MarkerType.CONDITION -> StitcherType.CONDITION_MARKER
    MarkerType.SWAP -> StitcherType.SWAP_MARKER
    StitcherTokenType.SCOPE_OPEN -> StitcherType.SCOPE_OPEN
    StitcherTokenType.SCOPE_CLOSE -> StitcherType.SCOPE_CLOSE
    StitcherTokenType.GROUP_OPEN -> StitcherType.GROUP_OPEN
    StitcherTokenType.GROUP_CLOSE -> StitcherType.GROUP_CLOSE
    StitcherTokenType.NEGATE -> StitcherType.NEGATE
    StitcherTokenType.ASSIGN -> StitcherType.ASSIGNMENT
    StitcherTokenType.AND -> StitcherType.AND
    StitcherTokenType.OR -> StitcherType.OR
    StitcherTokenType.EXPECT_WORD -> StitcherType.EXPECT_WORD
    StitcherTokenType.IDENTIFIER -> StitcherType.IDENTIFIER
    StitcherTokenType.PREDICATE -> StitcherType.PREDICATE
    StitcherTokenType.IF -> StitcherType.IF
    StitcherTokenType.ELSE -> StitcherType.ELSE
    StitcherTokenType.ELIF -> StitcherType.ELIF
    else -> ERROR_ELEMENT
}

fun IElementType.convert(): TokenType = when (this) {
    ERROR_ELEMENT -> NullType
    WHITE_SPACE -> WhitespaceType
    StitcherType.CONDITION_MARKER -> MarkerType.CONDITION
    StitcherType.SWAP_MARKER -> MarkerType.SWAP
    StitcherType.SCOPE_OPEN -> StitcherTokenType.SCOPE_OPEN
    StitcherType.SCOPE_CLOSE -> StitcherTokenType.SCOPE_CLOSE
    StitcherType.GROUP_OPEN -> StitcherTokenType.GROUP_OPEN
    StitcherType.GROUP_CLOSE -> StitcherTokenType.GROUP_CLOSE
    StitcherType.ASSIGNMENT -> StitcherTokenType.ASSIGN
    StitcherType.EXPECT_WORD -> StitcherTokenType.EXPECT_WORD
    StitcherType.IDENTIFIER -> StitcherTokenType.IDENTIFIER
    StitcherType.PREDICATE -> StitcherTokenType.PREDICATE
    StitcherType.IF -> StitcherTokenType.IF
    StitcherType.ELSE -> StitcherTokenType.ELSE
    StitcherType.ELIF -> StitcherTokenType.ELIF
    StitcherType.NEGATE -> StitcherTokenType.NEGATE
    StitcherType.AND -> StitcherTokenType.AND
    StitcherType.OR -> StitcherTokenType.OR
    else -> NullType
}