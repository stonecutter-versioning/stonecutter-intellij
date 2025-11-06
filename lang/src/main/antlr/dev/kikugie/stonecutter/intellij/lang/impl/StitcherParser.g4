parser grammar StitcherParser;

@header {
package dev.kikugie.stonecutter.intellij.lang.impl;
}

options { tokenVocab=StitcherLexer; }

/* PARSER */
definition
    : COND_MARK condition EOF   # conditionDefinition
    | SWAP_MARK swap EOF        # swapDefinition
    | REPL_MARK replacement EOF # replacementDefinition
    ;

scopeOpener
    : SCOPE_OPEN                  # closedScopeOpener
    | SCOPE_WORD (PLUS? literal)? # wordScopeOpener
    ;

replacement: IDENTIFIER;

swap
    : IDENTIFIER swapArguments? scopeOpener? # openerSwap
    | SCOPE_CLOSE                            # closerSwap
    ;

swapArguments: literal+;

condition
    : SUGAR_IF? conditionExpression scopeOpener?                                                                                    # openerCondition
    | SCOPE_CLOSE (((SUGAR_ELSE SUGAR_IF | SUGAR_ELIF | SUGAR_ELSE)? conditionExpression scopeOpener?) | (SUGAR_ELSE scopeOpener?)) # extensionCondition
    | SCOPE_CLOSE                                                                                                                   # closerCondition
    ;

conditionExpression
    : conditionExpression op=(OP_AND | OP_OR) conditionExpression # binaryExpression
    | OP_NOT conditionExpression                                  # unaryExpression
    | LEFT_BRACE conditionExpression RIGHT_BRACE                  # groupExpression
    | IDENTIFIER                                                  # constantExpression
    | (IDENTIFIER OP_ASSIGN)? versionPredicate+                   # assignmentExpression
    ;

versionPredicate
    : (stringComparator | semanticComparator)? semanticVersion # semanticPredicate
    | stringComparator stringVersion?                          # stringPredicate
    ;

semanticVersion
    : versionCore (DASH preRelease)? (PLUS buildMetadata)?
    ;

stringVersion
    : IDENTIFIER
    ;

semanticComparator
    : COMP_MINOR
    | COMP_MAJOR
    ;

stringComparator
    : COMP_EQUAL
    | COMP_NEQUAL
    | COMP_MORE
    | COMP_GMORE
    | COMP_LESS
    | COMP_GLESS
    ;

versionCore: NUMERIC (DOT NUMERIC)*;
preRelease: metadata (DOT metadata)*;
buildMetadata: metadata (DOT metadata)*;

metadata: NUMERIC | IDENTIFIER;
literal: IDENTIFIER | QUOTED;