package dev.kikugie.stonecutter.intellij.lang.impl;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

%%
%public
%class SwapTemplate
%implements FlexLexer
%unicode
%function advance
%type IElementType

NUMERIC = 0|[1-9][0-9]*
%%

<YYINITIAL> {
    "$" {NUMERIC} { return TokenType.DUMMY_HOLDER; }
    "\\" | "\$" | [^] { }
}