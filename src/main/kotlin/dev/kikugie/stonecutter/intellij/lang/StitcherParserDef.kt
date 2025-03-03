package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import dev.kikugie.stonecutter.intellij.lang.parsing.StitcherParser
import dev.kikugie.stonecutter.intellij.lang.token.StitcherElement

class StitcherParserDef : ParserDefinition {
    override fun createLexer(project: Project?): Lexer =StitcherLexer()
    override fun createParser(project: Project?): PsiParser = StitcherParser.Factory()
    override fun getFileNodeType(): IFileElementType = IFileElementType(StitcherLang)
    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY
    override fun getWhitespaceTokens(): TokenSet = TokenSet.WHITE_SPACE
    override fun getCommentTokens(): TokenSet = TokenSet.EMPTY
    override fun createElement(node: ASTNode): PsiElement = StitcherElement.map(node)
    override fun createFile(viewProvider: FileViewProvider): PsiFile = StitcherFile(viewProvider)
}