package dev.kikugie.stonecutter.intellij.lang

import com.intellij.psi.TokenType.ERROR_ELEMENT
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.data.token.MarkerType
import dev.kikugie.stitcher.data.token.WhitespaceType

import dev.kikugie.stitcher.data.token.TokenType as NativeType
import dev.kikugie.stitcher.data.token.StitcherTokenType as StitcherNativeType

abstract class StitcherType(name: String) : IElementType(name, StitcherLang)

class StitcherTokenType(name: String) : StitcherType(name) {
    companion object {
        val CONDITION_MARKER = StitcherTokenType("CONDITION_MARKER")
        val SWAP_MARKER = StitcherTokenType("SWAP_MARKER")
        val IF = StitcherTokenType("IF")
        val ELSE = StitcherTokenType("ELSE")
        val ELIF = StitcherTokenType("ELIF")
        val NEGATE = StitcherTokenType("NEGATE")
        val AND = StitcherTokenType("AND")
        val OR = StitcherTokenType("OR")
        val GROUP_OPEN = StitcherTokenType("LPAREN")
        val GROUP_CLOSE = StitcherTokenType("RPAREN")
        val EXPECT_WORD = StitcherTokenType("DOUBLE_ARROW")
        val SCOPE_OPEN = StitcherTokenType("LBRACE")
        val SCOPE_CLOSE = StitcherTokenType("RBRACE")
        val IDENTIFIER = StitcherTokenType("IDENTIFIER")
        val ASSIGNMENT = StitcherTokenType("ASSIGN")
        val PREDICATE = StitcherTokenType("PREDICATE")

        val DEPENDENCY_ID = StitcherComponentType("DEPENDENCY_ID")
        val CONSTANT_ID = StitcherComponentType("CONSTANT_ID")
        val SWAP_ID = StitcherComponentType("SWAP_ID")
    }
}

class StitcherComponentType(name: String) : StitcherType(name) {
    companion object {
        val DEFINITION = StitcherComponentType("DEFINITION")
        val SWAP = StitcherComponentType("SWAP")
        val CONDITION = StitcherComponentType("CONDITION")
        val CONDITION_SUGAR = StitcherComponentType("CONDITION_SUGAR")
        val CONDITION_EXPRESSION = StitcherComponentType("CONDITION_EXPRESSION")
        val GROUP_ENTRY = StitcherComponentType("GROUP_ENTRY")
        val UNARY_ENTRY = StitcherComponentType("UNARY_ENTRY")
        val BOOLEAN_ENTRY = StitcherComponentType("BOOLEAN_ENTRY")
        val ASSIGNMENT_ENTRY = StitcherComponentType("ASSIGNMENT_ENTRY")
        val PREDICATE_ENTRY = StitcherComponentType("PREDICATE_ENTRY")
    }
}

fun NativeType.convert(): IElementType = when (this) {
    WhitespaceType -> WHITE_SPACE
    MarkerType.CONDITION -> StitcherTokenType.CONDITION_MARKER
    MarkerType.SWAP -> StitcherTokenType.SWAP_MARKER
    StitcherNativeType.SCOPE_OPEN -> StitcherTokenType.SCOPE_OPEN
    StitcherNativeType.SCOPE_CLOSE -> StitcherTokenType.SCOPE_CLOSE
    StitcherNativeType.GROUP_OPEN -> StitcherTokenType.GROUP_OPEN
    StitcherNativeType.GROUP_CLOSE -> StitcherTokenType.GROUP_CLOSE
    StitcherNativeType.NEGATE -> StitcherTokenType.NEGATE
    StitcherNativeType.ASSIGN -> StitcherTokenType.ASSIGNMENT
    StitcherNativeType.AND -> StitcherTokenType.AND
    StitcherNativeType.OR -> StitcherTokenType.OR
    StitcherNativeType.EXPECT_WORD -> StitcherTokenType.EXPECT_WORD
    StitcherNativeType.IDENTIFIER -> StitcherTokenType.IDENTIFIER
    StitcherNativeType.PREDICATE -> StitcherTokenType.PREDICATE
    StitcherNativeType.IF -> StitcherTokenType.IF
    StitcherNativeType.ELSE -> StitcherTokenType.ELSE
    StitcherNativeType.ELIF -> StitcherTokenType.ELIF
    else -> ERROR_ELEMENT
}