lexer grammar StitcherLexer;

@header {
package dev.kikugie.stonecutter.intellij.lang.impl;
}

fragment NUMBER: '0'|[1-9][0-9]*;
fragment IDENTIFIER_START: [_a-zA-Z];
fragment IDENTIFIER_PART: [_\-a-zA-Z0-9];

fragment ESC_SLASH: '\\\\';
fragment ESC_STAR: '\\*';
fragment ESC_TICK: '\\\'';

COND_MARK: '?' -> mode(IN_DEFINITION);
SWAP_MARK: '$' -> mode(IN_DEFINITION);
REPL_MARK: '~' -> mode(IN_DEFINITION);

G_BAD_CHAR: . -> type(BAD_CHAR), channel(HIDDEN);

mode IN_DEFINITION;
DOT: '.';
DASH: '-';
PLUS: '+';

LEFT_BRACE: '(';
RIGHT_BRACE: ')';

SCOPE_CLOSE: '}';
SCOPE_OPEN: '{';
SCOPE_WORD: '>>';

SUGAR_IF: 'if';
SUGAR_ELSE: 'else';
SUGAR_ELIF: 'elif';

COMMON_COMP: '='|'!='|'>'|'>='|'<'|'<=';
SEMVER_COMP: '~'|'^';

OP_ASSIGN: ':';
OP_NOT: '!';
OP_AND: '&&';
OP_OR: '||';

NUMERIC: NUMBER;
IDENTIFIER: IDENTIFIER_START IDENTIFIER_PART*;
QUOTED: '\'' (ESC_SLASH | ESC_TICK | ~[\\'] )* '\'';
COMMENT: '*' (ESC_SLASH | ESC_STAR | ~[\\*])* '*' -> channel(HIDDEN);
WHITESPACE: [ \t]+ -> channel(HIDDEN);
BAD_CHAR: . -> channel(HIDDEN);
