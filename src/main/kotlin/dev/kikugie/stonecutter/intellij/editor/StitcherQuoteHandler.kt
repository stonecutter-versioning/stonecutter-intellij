package dev.kikugie.stonecutter.intellij.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes

class StitcherQuoteHandler : SimpleTokenSetQuoteHandler(StitcherTokenTypes.QUOTED)