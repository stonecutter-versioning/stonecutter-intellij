package dev.kikugie.stonecutter.intellij.lang.impl

object StitcherParserExtras {
    private const val OFFSET: Int = 12

    const val RULE_conditionExpression_binary: Int = 0 + OFFSET
    const val RULE_conditionExpression_unary: Int = 1 + OFFSET
    const val RULE_conditionExpression_group: Int = 2 + OFFSET
    const val RULE_conditionExpression_constant: Int = 3 + OFFSET
    const val RULE_conditionExpression_assignment: Int = 4 + OFFSET

    const val RULE_versionPredicate_semantic: Int = 5 + OFFSET
    const val RULE_versionPredicate_string: Int = 6 + OFFSET

    @JvmField val extraRuleNames: Array<String> = arrayOf(
        "conditionExpression:binary",
        "conditionExpression:unary",
        "conditionExpression:group",
        "conditionExpression:constant",
        "conditionExpression:assignment",
        "versionPredicate:semantic",
        "versionPredicate:string",
    )

    init {
        check(OFFSET == StitcherParser.ruleNames.size) { "Invalid extra rule offset!" }
    }
}