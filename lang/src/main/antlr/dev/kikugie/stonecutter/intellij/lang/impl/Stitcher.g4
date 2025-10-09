grammar Stitcher;

@header {
package dev.kikugie.stonecutter.intellij.lang.impl;
}

tokens {
    VERSION_OPERATOR
}

/* PARSER */
definition
    : COND_MARK condition EOF
    | SWAP_MARK swap EOF
    | REPL_MARK replacement EOF
    ;

scopeOpener: SCOPE_OPEN | SCOPE_WORD;

replacement: IDENTIFIER;

swap
    : IDENTIFIER swapArguments scopeOpener?
    | SCOPE_CLOSE
    ;

swapArguments: (IDENTIFIER | QUOTED)*;

condition
    : SUGAR_IF? conditionExpression scopeOpener?
    | SCOPE_CLOSE
      (
        (SUGAR_ELSE SUGAR_IF | SUGAR_ELIF | SUGAR_ELSE)? conditionExpression scopeOpener?
        | SUGAR_ELSE scopeOpener?
        |
      )
    ;

conditionExpression
    : conditionExpression (OP_AND | OP_OR) conditionExpression # binary
    | OP_NOT conditionExpression                               # unary
    | LEFT_BRACE conditionExpression RIGHT_BRACE               # group
    | IDENTIFIER                                               # constant
    | (IDENTIFIER OP_ASSIGN)? versionPredicate+                # assignment
    ;

versionPredicate
    : (semanticComparator | stringComparator)? LOOSE_VERSION # semantic
    | stringComparator IDENTIFIER?                           # string
    ;

semanticComparator
    : REPL_MARK
    | COMP_MAJOR
    ;

stringComparator
    : OP_NOT? COMP_EQUAL
    | COMP_MORE COMP_EQUAL?
    | COMP_LESS COMP_EQUAL?
    ;

/* LEXER */
fragment IDENTIFIER_START: [_a-zA-Z];
fragment IDENTIFIER_PART: [_\-a-zA-Z0-9];

fragment VERSION_START: [0-9];
fragment VERSION_PART: IDENTIFIER_PART | '+' | '-' | '.';

fragment ESC_SLASH: '\\\\';
fragment ESC_STAR: '\\*';
fragment ESC_TICK: '\\\'';

COND_MARK: '?';
SWAP_MARK: '$';
REPL_MARK: '~';

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

COMP_MAJOR: '^';
COMP_EQUAL: '=';
COMP_MORE: '>';
COMP_LESS: '<';

OP_ASSIGN: ':';
OP_NOT: '!';
OP_AND: '&&';
OP_OR: '||';

IDENTIFIER: IDENTIFIER_START IDENTIFIER_PART*;
LOOSE_VERSION: VERSION_START VERSION_PART*;
QUOTED: '\'' (ESC_SLASH | ESC_TICK | ~[\\'] )* '\'';
COMMENT: '*' (ESC_SLASH | ESC_STAR | ~[\\*])* '*' -> channel(HIDDEN);
WHITESPACE: [ \t]+ -> channel(HIDDEN);
