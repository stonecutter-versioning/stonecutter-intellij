package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import dev.kikugie.commons.takeAs
import dev.kikugie.stonecutter.intellij.lang.impl.*
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherCompositeType.*
import dev.kikugie.stonecutter.intellij.lang.psi.*
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory
import org.antlr.intellij.adaptor.lexer.TokenIElementType
import org.antlr.intellij.adaptor.parser.ANTLRParseTreeToPSIConverter
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree

private val LITERALS: TokenSet = PSIElementTypeFactory.createTokenSet(StitcherLang, StitcherLexer.QUOTED)
private val COMMENTS: TokenSet = PSIElementTypeFactory.createTokenSet(StitcherLang, StitcherLexer.COMMENT)
private val WHITESPACES: TokenSet = PSIElementTypeFactory.createTokenSet(StitcherLang, StitcherLexer.WHITESPACE)
private val LOGGER = Logger.getInstance(StitcherParserDef::class.java)

class StitcherParserDef : ParserDefinition {
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
        is CompositeIElementType -> mapRule(type.value, node)
        else -> {
            LOGGER.debug("Unknown Stitcher token type: $type")
            PsiStitcherNodeImpl(node)
        }
    }

    private fun mapRule(type: StitcherCompositeType, node: ASTNode): PsiStitcherNode = when (type) {
        COND, SWAP, REPL -> PsiCode(node)
        COND_OPEN -> PsiCondition.Opener(node)
        COND_EXT -> PsiCondition.Extension(node)
        COND_CLOSE -> PsiCondition.Closer(node)
        SWAP_OPEN -> PsiSwap.Opener(node)
        SWAP_CLOSE -> PsiSwap.Closer(node)
        SWAP_LOCAL -> PsiSwap.Closer(node)
        SWAP_EXPR -> PsiStitcherNodeImpl(node) // TODO
        REPL_TOGGLE -> PsiReplacement.Toggle(node)
        REPL_OPEN -> PsiReplacement.Local(node)
        REPL_CLOSE -> PsiReplacement.Closer(node)
        REPL_ENTRY -> PsiReplacement.Entry(node)
        GROUP -> PsiExpression.Group(node)
        UNARY -> PsiExpression.Unary(node)
        BINARY -> PsiExpression.Binary(node)
        CONSTANT -> PsiExpression.Constant(node)
        ASSIGNMENT -> PsiExpression.Assignment(node)
        CLOSED_SCOPE -> PsiScope.Closed(node)
        LOOKUP_SCOPE -> PsiScope.Lookup(node)
        SEM_PRED, STR_PRED -> PsiPredicate(node)
        SEM_VER -> PsiVersion.Semantic(node)
        STR_VER -> PsiVersion.String(node)
        SEM_CORE, SEM_PRE, SEM_BUILD -> PsiStitcherNodeImpl(node)
    }
}