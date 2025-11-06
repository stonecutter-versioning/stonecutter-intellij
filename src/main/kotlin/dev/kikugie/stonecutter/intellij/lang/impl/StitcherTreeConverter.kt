package dev.kikugie.stonecutter.intellij.lang.impl

import com.intellij.lang.PsiBuilder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import org.antlr.intellij.adaptor.parser.ANTLRParseTreeToPSIConverter
import org.antlr.v4.runtime.ParserRuleContext

private inline fun checked(action: () -> Unit) {
    ProgressIndicatorProvider.checkCanceled()
    action()
}

@Suppress("PublicApiImplicitType")
class StitcherTreeConverter(parser: StitcherParser, builder: PsiBuilder) :
    ANTLRParseTreeToPSIConverter(StitcherLang, parser, builder),
    StitcherParserListener {
    // Prevent duplicates
    override fun enterEveryRule(ctx: ParserRuleContext?) = Unit
    override fun exitEveryRule(ctx: ParserRuleContext?) = Unit

    override fun enterConditionDefinition(ctx: StitcherParser.ConditionDefinitionContext) = mark()
    override fun exitConditionDefinition(ctx: StitcherParser.ConditionDefinitionContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_conditionDefinition])

    override fun enterSwapDefinition(ctx: StitcherParser.SwapDefinitionContext) = mark()
    override fun exitSwapDefinition(ctx: StitcherParser.SwapDefinitionContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_swapDefinition])

    override fun enterReplacementDefinition(ctx: StitcherParser.ReplacementDefinitionContext) = mark()
    override fun exitReplacementDefinition(ctx: StitcherParser.ReplacementDefinitionContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_replacementDefinition])

    override fun enterClosedScopeOpener(ctx: StitcherParser.ClosedScopeOpenerContext) = mark()
    override fun exitClosedScopeOpener(ctx: StitcherParser.ClosedScopeOpenerContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_closedScopeOpener])

    override fun enterWordScopeOpener(ctx: StitcherParser.WordScopeOpenerContext) = mark()
    override fun exitWordScopeOpener(ctx: StitcherParser.WordScopeOpenerContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_wordScopeOpener])

    override fun enterReplacement(ctx: StitcherParser.ReplacementContext) = mark()
    override fun exitReplacement(ctx: StitcherParser.ReplacementContext) = release(ctx)

    override fun enterOpenerSwap(ctx: StitcherParser.OpenerSwapContext) = mark()
    override fun exitOpenerSwap(ctx: StitcherParser.OpenerSwapContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_openerSwap])

    override fun enterCloserSwap(ctx: StitcherParser.CloserSwapContext) = mark()
    override fun exitCloserSwap(ctx: StitcherParser.CloserSwapContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_closerSwap])

    override fun enterSwapArguments(ctx: StitcherParser.SwapArgumentsContext) = mark()
    override fun exitSwapArguments(ctx: StitcherParser.SwapArgumentsContext) = release(ctx)

    override fun enterOpenerCondition(ctx: StitcherParser.OpenerConditionContext) = mark()
    override fun exitOpenerCondition(ctx: StitcherParser.OpenerConditionContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_openerCondition])

    override fun enterExtensionCondition(ctx: StitcherParser.ExtensionConditionContext) = mark()
    override fun exitExtensionCondition(ctx: StitcherParser.ExtensionConditionContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_extensionCondition])

    override fun enterCloserCondition(ctx: StitcherParser.CloserConditionContext) = mark()
    override fun exitCloserCondition(ctx: StitcherParser.CloserConditionContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_closerCondition])

    override fun enterAssignmentExpression(ctx: StitcherParser.AssignmentExpressionContext) = mark()
    override fun exitAssignmentExpression(ctx: StitcherParser.AssignmentExpressionContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_assignmentExpression])

    override fun enterUnaryExpression(ctx: StitcherParser.UnaryExpressionContext) = mark()
    override fun exitUnaryExpression(ctx: StitcherParser.UnaryExpressionContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_unaryExpression])

    override fun enterGroupExpression(ctx: StitcherParser.GroupExpressionContext) = mark()
    override fun exitGroupExpression(ctx: StitcherParser.GroupExpressionContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_groupExpression])

    override fun enterConstantExpression(ctx: StitcherParser.ConstantExpressionContext) = mark()
    override fun exitConstantExpression(ctx: StitcherParser.ConstantExpressionContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_constantExpression])

    override fun enterBinaryExpression(ctx: StitcherParser.BinaryExpressionContext) = mark()
    override fun exitBinaryExpression(ctx: StitcherParser.BinaryExpressionContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_binaryExpression])

    override fun enterSemanticPredicate(ctx: StitcherParser.SemanticPredicateContext) = mark()
    override fun exitSemanticPredicate(ctx: StitcherParser.SemanticPredicateContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_semanticPredicate])

    override fun enterStringPredicate(ctx: StitcherParser.StringPredicateContext) = mark()
    override fun exitStringPredicate(ctx: StitcherParser.StringPredicateContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_stringPredicate])

    override fun enterSemanticVersion(ctx: StitcherParser.SemanticVersionContext) = mark()
    override fun exitSemanticVersion(ctx: StitcherParser.SemanticVersionContext) = release(ctx)

    override fun enterStringVersion(ctx: StitcherParser.StringVersionContext) = mark()
    override fun exitStringVersion(ctx: StitcherParser.StringVersionContext) = release(ctx)

    override fun enterVersionCore(ctx: StitcherParser.VersionCoreContext) = mark()
    override fun exitVersionCore(ctx: StitcherParser.VersionCoreContext) = release(ctx)

    override fun enterPreRelease(ctx: StitcherParser.PreReleaseContext) = mark()
    override fun exitPreRelease(ctx: StitcherParser.PreReleaseContext) = release(ctx)

    override fun enterBuildMetadata(ctx: StitcherParser.BuildMetadataContext) = mark()
    override fun exitBuildMetadata(ctx: StitcherParser.BuildMetadataContext) = release(ctx)

    override fun enterSemanticComparator(ctx: StitcherParser.SemanticComparatorContext) = Unit
    override fun exitSemanticComparator(ctx: StitcherParser.SemanticComparatorContext) = Unit

    override fun enterStringComparator(ctx: StitcherParser.StringComparatorContext) = Unit
    override fun exitStringComparator(ctx: StitcherParser.StringComparatorContext) = Unit

    // Don't box metadata
    override fun enterMetadata(ctx: StitcherParser.MetadataContext) = Unit
    override fun exitMetadata(ctx: StitcherParser.MetadataContext) = Unit

    override fun enterLiteral(ctx: StitcherParser.LiteralContext) = Unit
    override fun exitLiteral(ctx: StitcherParser.LiteralContext) = Unit

    private fun mark() = checked { markers.push(builder.mark()) }
    private fun release(ctx: ParserRuleContext) = release(ruleElementTypes[ctx.ruleIndex])
    private fun release(type: IElementType) = checked { markers.pop().done(type) }
}