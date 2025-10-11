parser grammar StitcherParser;

@header {
package dev.kikugie.stonecutter.intellij.lang.impl;
}

options { tokenVocab=StitcherLexer; }

/* PARSER */
definition
    : COND_MARK condition EOF
    | SWAP_MARK swap EOF
    | REPL_MARK replacement EOF
    ;

scopeOpener: SCOPE_OPEN | SCOPE_WORD;

replacement: IDENTIFIER;

swap
    : IDENTIFIER swapArguments? scopeOpener?
    | SCOPE_CLOSE
    ;

swapArguments: (IDENTIFIER | QUOTED)+;

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
    : (COMMON_COMP | SEMVER_COMP)? semanticVersion # semantic
    | COMMON_COMP stringVersion?                   # string
    ;

semanticVersion
    : versionCore (DASH preRelease)? (PLUS buildMetadata)?
    ;

stringVersion
    : IDENTIFIER
    ;

versionCore: NUMERIC (DOT NUMERIC)*;
preRelease: metadata (DOT metadata)*;
buildMetadata: metadata (DOT metadata)*;

metadata: NUMERIC | IDENTIFIER;