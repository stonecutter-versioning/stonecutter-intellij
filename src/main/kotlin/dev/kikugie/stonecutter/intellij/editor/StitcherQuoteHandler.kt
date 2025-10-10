package dev.kikugie.stonecutter.intellij.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser

class StitcherQuoteHandler : SimpleTokenSetQuoteHandler(StitcherLang.tokenTypeOf(StitcherParser.QUOTED))