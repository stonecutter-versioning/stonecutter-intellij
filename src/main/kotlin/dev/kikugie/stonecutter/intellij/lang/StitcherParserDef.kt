package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import dev.kikugie.commons.takeAs
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParserExtras
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherTreeConverter
import dev.kikugie.stonecutter.intellij.lang.psi.PsiCondition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.lang.psi.PsiPredicate
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory.defineLanguageIElementTypes
import org.antlr.intellij.adaptor.lexer.RuleIElementType
import org.antlr.intellij.adaptor.lexer.TokenIElementType
import org.antlr.intellij.adaptor.parser.ANTLRParseTreeToPSIConverter
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree

private val LITERALS: TokenSet = PSIElementTypeFactory.createTokenSet(StitcherLang, StitcherLexer.QUOTED)
private val COMMENTS: TokenSet = PSIElementTypeFactory.createTokenSet(StitcherLang, StitcherLexer.COMMENT)
private val WHITESPACES: TokenSet = PSIElementTypeFactory.createTokenSet(StitcherLang, StitcherLexer.WHITESPACE)

class StitcherParserDef : ParserDefinition {
    init {
        defineLanguageIElementTypes(StitcherLang, StitcherParser.VOCABULARY, StitcherParser.ruleNames + StitcherParserExtras.extraRuleNames)
    }

    override fun createLexer(project: Project?): Lexer = ANTLRLexerAdaptor(StitcherLang, StitcherLexer(null))
    override fun createParser(project: Project?): PsiParser = object : ANTLRParserAdaptor(StitcherLang, StitcherParser(null)) {
        override fun createListener(parser: Parser, root: IElementType, builder: PsiBuilder): ANTLRParseTreeToPSIConverter =
            StitcherTreeConverter(parser.takeAs(), builder)

        override fun parse(parser: Parser, root: IElementType): ParseTree =
            parser.takeAs<StitcherParser>().definition()
    }

    override fun getCommentTokens(): TokenSet = COMMENTS
    override fun getWhitespaceTokens(): TokenSet = WHITESPACES
    override fun getStringLiteralElements(): TokenSet = LITERALS
    override fun getFileNodeType(): IFileElementType = IFileElementType(StitcherLang)
    override fun createFile(viewProvider: FileViewProvider): PsiFile = StitcherFile(viewProvider)
    override fun createElement(node: ASTNode): PsiElement = when (val type = node.elementType) {
        is TokenIElementType -> ANTLRPsiNode(node)
        !is RuleIElementType -> ANTLRPsiNode(node)
        else -> mapRule(type, node)
    }

    private fun mapRule(type: RuleIElementType, node: ASTNode): PsiElement = when (type.ruleIndex) {
        StitcherParserExtras.RULE_conditionExpression_binary -> PsiExpression.Binary(node)
        StitcherParserExtras.RULE_conditionExpression_unary -> PsiExpression.Unary(node)
        StitcherParserExtras.RULE_conditionExpression_group -> PsiExpression.Group(node)
        StitcherParserExtras.RULE_conditionExpression_assignment -> PsiExpression.Assignment(node)
        StitcherParserExtras.RULE_conditionExpression_constant -> PsiExpression.Constant(node)

        StitcherParserExtras.RULE_versionPredicate_semantic -> PsiPredicate.Semantic(node)
        StitcherParserExtras.RULE_versionPredicate_string -> PsiPredicate.String(node)

        StitcherParser.RULE_definition -> PsiDefinition(node)
        StitcherParser.RULE_replacement -> PsiReplacement(node)
        StitcherParser.RULE_condition -> PsiCondition(node)
        StitcherParser.RULE_swap -> PsiSwap(node)
        StitcherParser.RULE_swapArguments -> PsiSwap.Args(node)

        else -> ANTLRPsiNode(node)
    }
}