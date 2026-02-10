package dev.kikugie.stonecutter.intellij.lang.impl

import com.intellij.lang.PsiBuilder
import com.intellij.openapi.progress.ProgressIndicatorProvider
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

    override fun enterConditionDefinition(ctx: StitcherParser.ConditionDefinitionContext?) = mark()
    override fun exitConditionDefinition(ctx: StitcherParser.ConditionDefinitionContext?) = release(StitcherCompositeType.COND)

    override fun enterSwapDefinition(ctx: StitcherParser.SwapDefinitionContext?) = mark()
    override fun exitSwapDefinition(ctx: StitcherParser.SwapDefinitionContext?) = release(StitcherCompositeType.SWAP)

    override fun enterReplacementDefinition(ctx: StitcherParser.ReplacementDefinitionContext?) = mark()
    override fun exitReplacementDefinition(ctx: StitcherParser.ReplacementDefinitionContext?) = release(StitcherCompositeType.REPL)

    override fun enterClosedScopeOpener(ctx: StitcherParser.ClosedScopeOpenerContext?) = mark()
    override fun exitClosedScopeOpener(ctx: StitcherParser.ClosedScopeOpenerContext?) = release(StitcherCompositeType.CLOSED_SCOPE)

    override fun enterWordScopeOpener(ctx: StitcherParser.WordScopeOpenerContext?) = mark()
    override fun exitWordScopeOpener(ctx: StitcherParser.WordScopeOpenerContext?) = release(StitcherCompositeType.LOOKUP_SCOPE)

    override fun enterElseSugar(ctx: StitcherParser.ElseSugarContext?) = Unit
    override fun exitElseSugar(ctx: StitcherParser.ElseSugarContext?) = Unit

    override fun enterToggleReplacement(ctx: StitcherParser.ToggleReplacementContext?) = mark()
    override fun exitToggleReplacement(ctx: StitcherParser.ToggleReplacementContext?) = release(StitcherCompositeType.REPL_TOGGLE)

    override fun enterLocalReplacement(ctx: StitcherParser.LocalReplacementContext?) = mark()
    override fun exitLocalReplacement(ctx: StitcherParser.LocalReplacementContext?) = release(StitcherCompositeType.REPL_OPEN)

    override fun enterCloserReplacement(ctx: StitcherParser.CloserReplacementContext?) = mark()
    override fun exitCloserReplacement(ctx: StitcherParser.CloserReplacementContext?) = release(StitcherCompositeType.REPL_CLOSE)

    override fun enterReplacementEntry(ctx: StitcherParser.ReplacementEntryContext?) = mark()
    override fun exitReplacementEntry(ctx: StitcherParser.ReplacementEntryContext?) = release(StitcherCompositeType.REPL_ENTRY)

    override fun enterCloserSwap(ctx: StitcherParser.CloserSwapContext?) = mark()
    override fun exitCloserSwap(ctx: StitcherParser.CloserSwapContext?) = release(StitcherCompositeType.SWAP_CLOSE)

    override fun enterIdentifierSwap(ctx: StitcherParser.IdentifierSwapContext?) = mark()
    override fun exitIdentifierSwap(ctx: StitcherParser.IdentifierSwapContext?) = release(StitcherCompositeType.SWAP_OPEN)

    override fun enterLocalSwap(ctx: StitcherParser.LocalSwapContext?) = mark()
    override fun exitLocalSwap(ctx: StitcherParser.LocalSwapContext?) = release(StitcherCompositeType.SWAP_LOCAL)

    override fun enterSwapArguments(ctx: StitcherParser.SwapArgumentsContext?): Unit = Unit
    override fun exitSwapArguments(ctx: StitcherParser.SwapArgumentsContext?): Unit = Unit
    
    override fun enterSwapExtension(ctx: StitcherParser.SwapExtensionContext?) = mark()
    override fun exitSwapExtension(ctx: StitcherParser.SwapExtensionContext?) = release(StitcherCompositeType.SWAP_EXPR)

    override fun enterOpenerCondition(ctx: StitcherParser.OpenerConditionContext?) = mark()
    override fun exitOpenerCondition(ctx: StitcherParser.OpenerConditionContext?) = release(StitcherCompositeType.COND_OPEN)

    override fun enterExtensionCondition(ctx: StitcherParser.ExtensionConditionContext?) = mark()
    override fun exitExtensionCondition(ctx: StitcherParser.ExtensionConditionContext?) = release(StitcherCompositeType.COND_EXT)

    override fun enterElseExtCondition(ctx: StitcherParser.ElseExtConditionContext?) = mark()
    override fun exitElseExtCondition(ctx: StitcherParser.ElseExtConditionContext?) = release(StitcherCompositeType.COND_EXT)

    override fun enterCloserCondition(ctx: StitcherParser.CloserConditionContext?) = mark()
    override fun exitCloserCondition(ctx: StitcherParser.CloserConditionContext?) = release(StitcherCompositeType.COND_CLOSE)

    override fun enterAssignmentExpression(ctx: StitcherParser.AssignmentExpressionContext?) = mark()
    override fun exitAssignmentExpression(ctx: StitcherParser.AssignmentExpressionContext?) = release(StitcherCompositeType.ASSIGNMENT)

    override fun enterUnaryExpression(ctx: StitcherParser.UnaryExpressionContext?) = mark()
    override fun exitUnaryExpression(ctx: StitcherParser.UnaryExpressionContext?) = release(StitcherCompositeType.UNARY)

    override fun enterGroupExpression(ctx: StitcherParser.GroupExpressionContext?) = mark()
    override fun exitGroupExpression(ctx: StitcherParser.GroupExpressionContext?) = release(StitcherCompositeType.BINARY)

    override fun enterConstantExpression(ctx: StitcherParser.ConstantExpressionContext?) = mark()
    override fun exitConstantExpression(ctx: StitcherParser.ConstantExpressionContext?) = release(StitcherCompositeType.CONSTANT)

    override fun enterBinaryExpression(ctx: StitcherParser.BinaryExpressionContext?) = mark()
    override fun exitBinaryExpression(ctx: StitcherParser.BinaryExpressionContext?) = release(StitcherCompositeType.BINARY)

    override fun enterSemanticPredicate(ctx: StitcherParser.SemanticPredicateContext?) = mark()
    override fun exitSemanticPredicate(ctx: StitcherParser.SemanticPredicateContext?) = release(StitcherCompositeType.SEM_PRED)

    override fun enterStringPredicate(ctx: StitcherParser.StringPredicateContext?) = mark()
    override fun exitStringPredicate(ctx: StitcherParser.StringPredicateContext?) = release(StitcherCompositeType.STR_PRED)

    override fun enterSemanticVersion(ctx: StitcherParser.SemanticVersionContext?) = mark()
    override fun exitSemanticVersion(ctx: StitcherParser.SemanticVersionContext?) = release(StitcherCompositeType.SEM_VER)

    override fun enterStringVersion(ctx: StitcherParser.StringVersionContext?) = mark()
    override fun exitStringVersion(ctx: StitcherParser.StringVersionContext?) = release(StitcherCompositeType.STR_VER)

    override fun enterVersionCore(ctx: StitcherParser.VersionCoreContext?) = mark()
    override fun exitVersionCore(ctx: StitcherParser.VersionCoreContext?) = release(StitcherCompositeType.SEM_CORE)

    override fun enterPreRelease(ctx: StitcherParser.PreReleaseContext?) = mark()
    override fun exitPreRelease(ctx: StitcherParser.PreReleaseContext?) = release(StitcherCompositeType.SEM_PRE)

    override fun enterBuildMetadata(ctx: StitcherParser.BuildMetadataContext?) = mark()
    override fun exitBuildMetadata(ctx: StitcherParser.BuildMetadataContext?) = release(StitcherCompositeType.SEM_BUILD)

    override fun enterSemanticComparator(ctx: StitcherParser.SemanticComparatorContext?) = Unit
    override fun exitSemanticComparator(ctx: StitcherParser.SemanticComparatorContext?) = Unit

    override fun enterStringComparator(ctx: StitcherParser.StringComparatorContext?) = Unit
    override fun exitStringComparator(ctx: StitcherParser.StringComparatorContext?) = Unit

    // Don't box metadata
    override fun enterMetadata(ctx: StitcherParser.MetadataContext?) = Unit
    override fun exitMetadata(ctx: StitcherParser.MetadataContext?) = Unit

    override fun enterLiteral(ctx: StitcherParser.LiteralContext?) = Unit
    override fun exitLiteral(ctx: StitcherParser.LiteralContext?) = Unit

    private fun mark(): Unit = checked { markers.push(builder.mark()) }
    private fun release(type: StitcherCompositeType): Unit = checked { markers.pop().done(type.asIElementType()) }
}