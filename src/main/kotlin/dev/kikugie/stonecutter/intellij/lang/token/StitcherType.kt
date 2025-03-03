package dev.kikugie.stonecutter.intellij.lang.token

import com.intellij.psi.TokenType.ERROR_ELEMENT
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.data.token.WhitespaceType
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stitcher.data.token.StitcherTokenType as StitcherNativeType
import dev.kikugie.stitcher.data.token.TokenType as NativeType

open class StitcherType(name: String) : IElementType(name, StitcherLang) {
    class Marker(name: String) : StitcherType("StitcherMarker:$name") {
        companion object {
            val CONDITION = Marker("CONDITION")
            val SWAP = Marker("SWAP")
        }
    }

    class Sugar(name: String) : StitcherType("StitcherSugar:$name") {
        companion object {
            val IF = Sugar("IF")
            val ELSE = Sugar("ELSE")
            val ELIF = Sugar("ELIF")
        }
    }

    class Operator(name: String) : StitcherType("StitcherOperator:$name") {
        companion object {
            val NOT = Operator("NOT")
            val AND = Operator("AND")
            val OR = Operator("OR")

            val ASSIGN = Operator("ASSIGN")
            val LPAREN = Operator("LPAREN")
            val RPAREN = Operator("RPAREN")
        }
    }

    class Scope(name: String) : StitcherType("StitcherScope:$name") {
        companion object {
            val OPEN = Scope("OPEN")
            val CLOSE = Scope("CLOSE")
            val WORD = Scope("WORD")
        }
    }

    class Primitive(name: String) : StitcherType("StitcherPrimitive:$name") {
        companion object {
            val IDENTIFIER = Primitive("IDENTIFIER")
            val PREDICATE = Primitive("PREDICATE")
        }
    }

    class Reference(name: String) : StitcherType("StitcherReference:$name") {
        companion object {
            val AMBIGUOUS = Reference("AMBIGUOUS")
            val CONSTANT = Reference("CONSTANT")
            val DEPENDENCY = Reference("DEPENDENCY")
            val SWAP = Reference("SWAP")
        }
    }

    class Component(name: String) : StitcherType("StitcherComponent:$name") {
        companion object {
            val DEFINITION = Component("DEFINITION")
            val SWAP = Component("SWAP")
            val CONDITION = Component("CONDITION")
            val SUGAR = Component("CONDITION_SUGAR")
            val EXPRESSION = Component("CONDITION_EXPRESSION")
            val GROUP = Component("GROUP")
            val BINARY = Component("BINARY")
            val UNARY = Component("UNARY")
            val ASSIGNMENT = Component("ASSIGNMENT")
            val PREDICATE = Component("PREDICATE")
        }
    }


    companion object {
        fun convert(native: NativeType): IElementType = when (native) {
            WhitespaceType -> WHITE_SPACE
            dev.kikugie.stitcher.data.token.MarkerType.CONDITION -> Marker.CONDITION
            dev.kikugie.stitcher.data.token.MarkerType.SWAP -> Marker.SWAP
            StitcherNativeType.SCOPE_OPEN -> Scope.OPEN
            StitcherNativeType.SCOPE_CLOSE -> Scope.CLOSE
            StitcherNativeType.GROUP_OPEN -> Operator.LPAREN
            StitcherNativeType.GROUP_CLOSE -> Operator.RPAREN
            StitcherNativeType.NEGATE -> Operator.NOT
            StitcherNativeType.ASSIGN -> Operator.ASSIGN
            StitcherNativeType.AND -> Operator.AND
            StitcherNativeType.OR -> Operator.OR
            StitcherNativeType.EXPECT_WORD -> Scope.WORD
            StitcherNativeType.IDENTIFIER -> Primitive.IDENTIFIER
            StitcherNativeType.PREDICATE -> Primitive.PREDICATE
            StitcherNativeType.IF -> Sugar.IF
            StitcherNativeType.ELSE -> Sugar.ELSE
            StitcherNativeType.ELIF -> Sugar.ELIF
            else -> ERROR_ELEMENT
        }
    }
}