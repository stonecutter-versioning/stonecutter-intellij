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

    override fun enterDefinition(ctx: StitcherParser.DefinitionContext) = mark()
    override fun exitDefinition(ctx: StitcherParser.DefinitionContext) = release(ctx)

    override fun enterReplacement(ctx: StitcherParser.ReplacementContext) = mark()
    override fun exitReplacement(ctx: StitcherParser.ReplacementContext) = release(ctx)

    override fun enterSwap(ctx: StitcherParser.SwapContext) = mark()
    override fun exitSwap(ctx: StitcherParser.SwapContext) = release(ctx)

    override fun enterCondition(ctx: StitcherParser.ConditionContext) = mark()
    override fun exitCondition(ctx: StitcherParser.ConditionContext) = release(ctx)

    // Don't box the opener
    override fun enterScopeOpener(ctx: StitcherParser.ScopeOpenerContext) = Unit
    override fun exitScopeOpener(ctx: StitcherParser.ScopeOpenerContext) = Unit

    override fun enterSwapArguments(ctx: StitcherParser.SwapArgumentsContext) = mark()
    override fun exitSwapArguments(ctx: StitcherParser.SwapArgumentsContext) = release(ctx)

    override fun enterAssignment(ctx: StitcherParser.AssignmentContext) = mark()
    override fun exitAssignment(ctx: StitcherParser.AssignmentContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_conditionExpression_assignment])

    override fun enterUnary(ctx: StitcherParser.UnaryContext) = mark()
    override fun exitUnary(ctx: StitcherParser.UnaryContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_conditionExpression_unary])

    override fun enterGroup(ctx: StitcherParser.GroupContext) = mark()
    override fun exitGroup(ctx: StitcherParser.GroupContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_conditionExpression_group])

    override fun enterConstant(ctx: StitcherParser.ConstantContext) = mark()
    override fun exitConstant(ctx: StitcherParser.ConstantContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_conditionExpression_constant])

    override fun enterBinary(ctx: StitcherParser.BinaryContext) = mark()
    override fun exitBinary(ctx: StitcherParser.BinaryContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_conditionExpression_binary])

    override fun enterSemantic(ctx: StitcherParser.SemanticContext) = mark()
    override fun exitSemantic(ctx: StitcherParser.SemanticContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_versionPredicate_semantic])

    override fun enterString(ctx: StitcherParser.StringContext) = mark()
    override fun exitString(ctx: StitcherParser.StringContext) =
        release(ruleElementTypes[StitcherParserExtras.RULE_versionPredicate_string])

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

    // Don't box metadata
    override fun enterMetadata(ctx: StitcherParser.MetadataContext) = Unit
    override fun exitMetadata(ctx: StitcherParser.MetadataContext) = Unit

    private fun mark() = checked { markers.push(builder.mark()) }
    private fun release(ctx: ParserRuleContext) = release(ruleElementTypes[ctx.ruleIndex])
    private fun release(type: IElementType) = checked { markers.pop().done(type) }
}