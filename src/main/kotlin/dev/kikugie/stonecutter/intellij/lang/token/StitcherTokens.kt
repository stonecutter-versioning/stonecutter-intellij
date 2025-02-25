package dev.kikugie.stonecutter.intellij.lang.token

import com.intellij.psi.TokenType.ERROR_ELEMENT
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.data.token.MarkerType
import dev.kikugie.stitcher.data.token.WhitespaceType

import dev.kikugie.stitcher.data.token.TokenType as NativeType
import dev.kikugie.stitcher.data.token.StitcherTokenType as StitcherNativeType

fun NativeType.convert(): IElementType = when (this) {
    WhitespaceType -> WHITE_SPACE
    MarkerType.CONDITION -> StitcherTypes.Marker.CONDITION
    MarkerType.SWAP -> StitcherTypes.Marker.SWAP
    StitcherNativeType.SCOPE_OPEN -> StitcherTypes.Scope.OPEN
    StitcherNativeType.SCOPE_CLOSE -> StitcherTypes.Scope.CLOSE
    StitcherNativeType.GROUP_OPEN -> StitcherTypes.Operator.LPAREN
    StitcherNativeType.GROUP_CLOSE -> StitcherTypes.Operator.RPAREN
    StitcherNativeType.NEGATE -> StitcherTypes.Operator.NOT
    StitcherNativeType.ASSIGN -> StitcherTypes.Operator.ASSIGN
    StitcherNativeType.AND -> StitcherTypes.Operator.AND
    StitcherNativeType.OR -> StitcherTypes.Operator.OR
    StitcherNativeType.EXPECT_WORD -> StitcherTypes.Scope.WORD
    StitcherNativeType.IDENTIFIER -> StitcherTypes.Primitive.IDENTIFIER
    StitcherNativeType.PREDICATE -> StitcherTypes.Primitive.PREDICATE
    StitcherNativeType.IF -> StitcherTypes.Sugar.IF
    StitcherNativeType.ELSE -> StitcherTypes.Sugar.ELSE
    StitcherNativeType.ELIF -> StitcherTypes.Sugar.ELIF
    else -> ERROR_ELEMENT
}