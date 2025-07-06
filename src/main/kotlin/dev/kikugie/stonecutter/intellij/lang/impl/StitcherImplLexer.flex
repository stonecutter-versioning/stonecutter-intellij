package dev.kikugie.stonecutter.intellij.lang.impl;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes;

%%

%public
%class StitcherImplLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

LETTER      = [a-zA-Z]
DIGIT       = [0-9]
NON_DIGIT   = -|[_+\.]|{LETTER}
SYMBOL      = {DIGIT}|{NON_DIGIT}

NUMBER      = 0|([1-9]{DIGIT}*)

%state DEFINITION

%%

<YYINITIAL> \? { yybegin(DEFINITION); return StitcherTokenTypes.COND_MARKER; }
<YYINITIAL> \$ { yybegin(DEFINITION); return StitcherTokenTypes.SWAP_MARKER; }
<YYINITIAL> \~ { yybegin(DEFINITION); return StitcherTokenTypes.REPL_MARKER; }

<DEFINITION> \. { return StitcherTokenTypes.DOT; }
<DEFINITION> \- { return StitcherTokenTypes.DASH; }
<DEFINITION> \+ { return StitcherTokenTypes.PLUS; }

<DEFINITION> "{"|">>" { return StitcherTokenTypes.OPENER; }
<DEFINITION> "}" { return StitcherTokenTypes.CLOSER; }
<DEFINITION> "if"|"else"|"elif" { return StitcherTokenTypes.SUGAR; }
<DEFINITION> "&&"|"||" { return StitcherTokenTypes.BINARY; }
<DEFINITION> "!" { return StitcherTokenTypes.UNARY; }
<DEFINITION> ":" { return StitcherTokenTypes.ASSIGN; }
<DEFINITION> "(" { return StitcherTokenTypes.LEFT_BRACE; }
<DEFINITION> ")" { return StitcherTokenTypes.RIGHT_BRACE; }
<DEFINITION> "="|">"|"<"|">="|"<="|"~"|"^" { return StitcherTokenTypes.COMPARATOR; }

<DEFINITION> ({LETTER}{SYMBOL}*) { return StitcherTokenTypes.LITERAL; }
<DEFINITION> ({NUMBER}) { return StitcherTokenTypes.NUMERIC; }
<DEFINITION> ({LETTER}{SYMBOL}*)|({NUMBER}*) { return StitcherTokenTypes.IDENTIFIER; }

[ \t]+ { return TokenType.WHITE_SPACE; }
[^] { return TokenType.BAD_CHARACTER; }