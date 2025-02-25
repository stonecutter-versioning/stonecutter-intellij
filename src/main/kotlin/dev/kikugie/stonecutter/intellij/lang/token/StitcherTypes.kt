package dev.kikugie.stonecutter.intellij.lang.token

import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.StitcherLang

open class StitcherType(name: String) : IElementType(name, StitcherLang)
class StitcherMarkerType(name: String) : StitcherType(name)
class StitcherConditionSugarType(name: String) : StitcherType(name)
class StitcherOperatorType(name: String) : StitcherType(name)
class StitcherScopeType(name: String) : StitcherType(name)
class StitcherPrimitiveType(name: String) : StitcherType(name)
class StitcherComponentType(name: String) : StitcherType(name)

object StitcherTypes {
    val OUTER_ELEMENT = StitcherType("OUTER")
    val CONTENT_ELEMENT = StitcherType("CONTENT")

    object Marker {
        val CONDITION = StitcherMarkerType("CONDITION_MARKER")
        val SWAP = StitcherMarkerType("SWAP_MARKER")
    }

    object Sugar {
        val IF = StitcherConditionSugarType("IF_SUGAR")
        val ELSE = StitcherConditionSugarType("ELSE_SUGAR")
        val ELIF = StitcherConditionSugarType("ELIF_SUGAR")
    }

    object Operator {
        val NOT = StitcherOperatorType("NOT_OPERATOR")
        val AND = StitcherOperatorType("AND_OPERATOR")
        val OR = StitcherOperatorType("OR_OPERATOR")

        val ASSIGN = StitcherOperatorType("ASSIGN_OPERATOR")
        val LPAREN = StitcherOperatorType("LPAREN_OPERATOR")
        val RPAREN = StitcherOperatorType("RPAREN_OPERATOR")
    }

    object Scope {
        val OPEN = StitcherScopeType("SCOPE_OPEN")
        val CLOSE = StitcherScopeType("SCOPE_CLOSE")
        val WORD = StitcherScopeType("SCOPE_WORD")
    }

    object Primitive {
        val IDENTIFIER = StitcherPrimitiveType("IDENTIFIER")
        val PREDICATE = StitcherPrimitiveType("PREDICATE")

        val CONSTANT = StitcherPrimitiveType("CONSTANT")
        val DEPENDENCY = StitcherPrimitiveType("DEPENDENCY")
        val SWAP = StitcherPrimitiveType("SWAP")
    }

    object Component {
        val DEFINITION = StitcherComponentType("DEFINITION")
        val SWAP = StitcherComponentType("SWAP")
        val CONDITION = StitcherComponentType("CONDITION")
        val SUGAR = StitcherComponentType("CONDITION_SUGAR")
        val EXPRESSION = StitcherComponentType("CONDITION_EXPRESSION")
        val GROUP = StitcherComponentType("GROUP")
        val BOOLEAN = StitcherComponentType("BOOLEAN")
        val UNARY = StitcherComponentType("UNARY")
        val ASSIGNMENT = StitcherComponentType("ASSIGNMENT")
        val PREDICATE = StitcherComponentType("PREDICATE")
    }

    object Invalid : StitcherType("ERROR")
}