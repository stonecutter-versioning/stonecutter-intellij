package dev.kikugie.stonecutter.intellij.lang

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.LightPsiParser
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.jetbrains.rd.util.string.printToString
import dev.kikugie.stonecutter.intellij.lang.file.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.parsing.AstBuilder
import dev.kikugie.stonecutter.intellij.lang.parsing.ErrorHandlers
import dev.kikugie.stonecutter.intellij.lang.parsing.StitcherParser
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType

class StitcherParserDef : ParserDefinition {
    override fun createLexer(project: Project?): Lexer =StitcherLexer()
    override fun createParser(project: Project?): PsiParser = StitcherPsiParser()
    override fun getFileNodeType(): IFileElementType = IFileElementType(StitcherLang)
    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY
    override fun getWhitespaceTokens(): TokenSet = TokenSet.WHITE_SPACE
    override fun getCommentTokens(): TokenSet = TokenSet.EMPTY
    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)
    override fun createFile(viewProvider: FileViewProvider): PsiFile = StitcherFile(viewProvider)

    private class StitcherPsiParser : PsiParser, LightPsiParser {
        override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
            builder.setDebugMode(true)
            parseLight(root, builder)
            return builder.treeBuilt
        }

        override fun parseLight(type: IElementType, builder: PsiBuilder) = with(builder) {
            val root = mark()
            try {
                createParser(this).parse()
                if (tokenType != null) {
                    val error = mark()
                    while (!eof()) advanceLexer()
                    error.error("Expected comment to end")
                }
                root.done(type)
            } catch (e: Throwable) {
                root.error(e.message ?: e.printToString())
            }
        }

        private fun createParser(builder: PsiBuilder) =
            StitcherParser(PsiAstBuilder(builder), ErrorHandlers { PsiHandler(it, builder) })
    }

    private class PsiAstBuilder(val builder: PsiBuilder) : AstBuilder {
        override val text: String? get() = builder.tokenText
        override val current: IElementType? get() = builder.tokenType

        override fun advance() = builder.advanceLexer()

        override fun reassign(type: IElementType) = builder.remapCurrentToken(type)

        override fun report(message: String) = builder.error(message)

        override fun peek(steps: Int): IElementType? = builder.lookAhead(steps)

        override fun wrap(type: StitcherType, action: (AstBuilder.Cancellable) -> Unit) = with(builder.mark()) {
            try {
                action(AstBuilder.Cancellable { drop(); throw DummyException })
                done(type)
            } catch (e: Throwable) {
                if (e !is DummyException)
                    error(e.message ?: e.printToString())
            }
        }
    }

    private class PsiHandler(val type: StitcherType?, val builder: PsiBuilder) : ErrorHandlers.Handler {
        var marker: PsiBuilder.Marker? = null
        lateinit var msg: String

        override fun mark(message: String) {
            if (marker != null) return
            marker = builder.mark()
            msg = message
        }

        override fun release() = marker?.run {
            if (type == null) error(msg)
            else done(type)
            marker = null
        } ?: Unit
    }

    private object DummyException : RuntimeException() {
        private fun readResolve(): Any = DummyException
    }
}