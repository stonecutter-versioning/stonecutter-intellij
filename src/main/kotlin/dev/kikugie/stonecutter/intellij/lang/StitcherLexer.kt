package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lexer.FlexAdapter
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherImplLexer

class StitcherLexer : FlexAdapter(StitcherImplLexer(null))
