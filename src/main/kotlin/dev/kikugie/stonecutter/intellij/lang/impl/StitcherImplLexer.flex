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

%state DEFINTION

%%

<YYINITIAL> \? { yybegin(DEFINTION); return StitcherTokenTypes.COND_MARKER; }
<YYINITIAL> \$ { yybegin(DEFINTION); return StitcherTokenTypes.SWAP_MARKER; }
<YYINITIAL> \~ { yybegin(DEFINTION); return StitcherTokenTypes.REPL_MARKER; }

<DEFINTION> \. { return StitcherTokenTypes.DOT; }
<DEFINTION> \- { return StitcherTokenTypes.DASH; }
<DEFINTION> \+ { return StitcherTokenTypes.PLUS; }

<DEFINTION> "{"|">>" { return StitcherTokenTypes.OPENER; }
<DEFINTION> "}" { return StitcherTokenTypes.CLOSER; }
<DEFINTION> "if"|"else"|"elif" { return StitcherTokenTypes.SUGAR; }
<DEFINTION> "&&"|"||" { return StitcherTokenTypes.BINARY; }
<DEFINTION> "!" { return StitcherTokenTypes.UNARY; }
<DEFINTION> ":" { return StitcherTokenTypes.ASSIGN; }
<DEFINTION> "(" { return StitcherTokenTypes.LEFT_BRACE; }
<DEFINTION> ")" { return StitcherTokenTypes.RIGHT_BRACE; }
<DEFINTION> "="|">"|"<"|">="|"<="|"~"|"^" { return StitcherTokenTypes.COMPARATOR; }

<DEFINTION> ({LETTER}{SYMBOL}*) { return StitcherTokenTypes.LITERAL; }
<DEFINTION> ({NUMBER}) { return StitcherTokenTypes.NUMERIC; }
<DEFINTION> ({LETTER}{SYMBOL}*)|({NUMBER}*) { return StitcherTokenTypes.IDENTIFIER; }

[ \t]+ { return TokenType.WHITE_SPACE; }
[^] { return TokenType.BAD_CHARACTER; }