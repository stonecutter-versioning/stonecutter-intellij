package dev.kikugie.stonecutter.intellij.lang.impl

@Suppress("ConstPropertyName")
object StitcherParserExtras {
    private const val OFFSET: Int = 17

    const val RULE_conditionDefinition: Int = OFFSET + 0
    const val RULE_swapDefinition: Int = OFFSET + 1
    const val RULE_replacementDefinition: Int = OFFSET + 2
    const val RULE_closedScopeOpener: Int = OFFSET + 3
    const val RULE_wordScopeOpener: Int = OFFSET + 4
    const val RULE_openerSwap: Int = OFFSET + 5
    const val RULE_closerSwap: Int = OFFSET + 6
    const val RULE_openerCondition: Int = OFFSET + 7
    const val RULE_extensionCondition: Int = OFFSET + 8
    const val RULE_closerCondition: Int = OFFSET + 9
    const val RULE_binaryExpression: Int = OFFSET + 10
    const val RULE_unaryExpression: Int = OFFSET + 11
    const val RULE_groupExpression: Int = OFFSET + 12
    const val RULE_constantExpression: Int = OFFSET + 13
    const val RULE_assignmentExpression: Int = OFFSET + 14
    const val RULE_semanticPredicate: Int = OFFSET + 15
    const val RULE_stringPredicate: Int = OFFSET + 16

    @JvmField val extraRuleNames: Array<String> = arrayOf(
        "conditionDefinition",
        "swapDefinition",
        "replacementDefinition",
        "closedScopeOpener",
        "wordScopeOpener",
        "openerSwap",
        "closerSwap",
        "openerCondition",
        "extensionCondition",
        "closerCondition",
        "binaryExpression",
        "unaryExpression",
        "groupExpression",
        "constantExpression",
        "assignmentExpression",
        "semanticPredicate",
        "stringPredicate",
    )

    init {
        check(OFFSET == StitcherParser.ruleNames.size) { "Invalid extra rule offset!" }
    }
}