package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lang.InjectableLanguage
import com.intellij.lang.Language
import com.intellij.psi.templateLanguages.TemplateLanguage
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory.*
import org.antlr.intellij.adaptor.lexer.RuleIElementType
import org.antlr.intellij.adaptor.lexer.TokenIElementType

object StitcherLang : Language("Stitcher"), InjectableLanguage, TemplateLanguage {
    private val tokenIElementTypes: Map<Int, TokenIElementType>
    private val ruleIElementTypes: Map<Int, RuleIElementType>

    init {
        defineLanguageIElementTypes(this, StitcherParser.VOCABULARY, StitcherParser.ruleNames)
        tokenIElementTypes = getTokenIElementTypes(this).associateBy(TokenIElementType::getANTLRTokenType)
        ruleIElementTypes = getRuleIElementTypes(this).associateBy(RuleIElementType::getRuleIndex)
    }

    fun tokenTypeOf(type: Int) = checkNotNull(tokenIElementTypes[type])
    fun ruleTypeOf(index: Int) = checkNotNull(ruleIElementTypes[index])

    private fun readResolve(): Any = StitcherLang
}