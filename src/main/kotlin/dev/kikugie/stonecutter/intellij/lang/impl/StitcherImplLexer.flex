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

NUMERIC = 0|[1-9][0-9]*
LITERAL = [a-zA-Z]+

IDENTIFIER_START = [_a-zA-Z]
IDENTIFIER_PART = [_\-a-zA-Z0-9]

ESC_SLASH = '\\\\';
ESC_STAR = '\\*';
ESC_TICK = '\\\'';

%state DEFINITION
%state VERSION
%%

<YYINITIAL> {
    \? { yybegin(DEFINITION); return StitcherTokenTypes.COND_MARKER; }
    \$ { yybegin(DEFINITION); return StitcherTokenTypes.SWAP_MARKER; }
    \~ { yybegin(DEFINITION); return StitcherTokenTypes.REPL_MARKER; }
}

<DEFINITION> {
    "'" ({ESC_SLASH} | {ESC_TICK} | [^'])* "'" { return StitcherTokenTypes.QUOTED; }
    "*" ({ESC_SLASH} | {ESC_STAR} | [^*])* "*" { return StitcherTokenTypes.COMMENT; }

    "(" { return StitcherTokenTypes.LEFT_BRACE; }
    ")" { return StitcherTokenTypes.RIGHT_BRACE; }

    "}" { return StitcherTokenTypes.CLOSER; }
    "{" { return StitcherTokenTypes.OPENER; }
    ">>" { return StitcherTokenTypes.WORD; }

    "if" { return StitcherTokenTypes.SUGAR_IF; }
    "elif" { return StitcherTokenTypes.SUGAR_ELIF; }
    "else" { return StitcherTokenTypes.SUGAR_ELSE; }

    "&&"|"||" { return StitcherTokenTypes.BINARY; }
    "!" { return StitcherTokenTypes.UNARY; }
    ":" { return StitcherTokenTypes.ASSIGN; }

    "="|">"|"<"|">="|"<="|"~"|"^" { return StitcherTokenTypes.COMPARATOR; }

    {IDENTIFIER_START}{IDENTIFIER_PART}* { return StitcherTokenTypes.IDENTIFIER; }
    {NUMERIC} { yybegin(VERSION); return StitcherTokenTypes.NUMERIC; }
}

<VERSION> {
    \. { return StitcherTokenTypes.DOT; }
    \- { return StitcherTokenTypes.DASH; }
    \+ { return StitcherTokenTypes.PLUS; }

    {NUMERIC} { return StitcherTokenTypes.NUMERIC; }
    {LITERAL} { return StitcherTokenTypes.LITERAL; }

    [^] { yypushback(1); yybegin(DEFINITION); }
}

[ \t]+ { return TokenType.WHITE_SPACE; }
[^] { return TokenType.BAD_CHARACTER; }