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

condition
    : SUGAR_IF? conditionExpression scopeOpener?              # openerCondition
    | SCOPE_CLOSE elseSugar? conditionExpression scopeOpener? # extensionCondition
    | SCOPE_CLOSE SUGAR_ELSE scopeOpener?                     # elseExtCondition
    | SCOPE_CLOSE                                             # closerCondition
    ;

swap
    : IDENTIFIER swapArguments? scopeOpener?                                              # identifierSwap
    | SUGAR_IF conditionExpression literal swapExtension* SUGAR_ELSE literal scopeOpener? # localSwap
    | SCOPE_CLOSE                                                                         # closerSwap
    ;

replacement
    : replacementEntry+                                                # toggleReplacement
    | SUGAR_IF conditionExpression literal OP_DIR literal scopeOpener? # localReplacement
    | SCOPE_CLOSE                                                      # closerReplacement
    ;

scopeOpener
    : SCOPE_OPEN                  # closedScopeOpener
    | SCOPE_WORD (PLUS? literal)? # wordScopeOpener
    ;

elseSugar: SUGAR_ELSE SUGAR_IF | SUGAR_ELIF | SUGAR_ELSE;

swapArguments: literal+;

swapExtension: elseSugar conditionExpression literal;

replacementEntry: OP_NOT? IDENTIFIER;

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