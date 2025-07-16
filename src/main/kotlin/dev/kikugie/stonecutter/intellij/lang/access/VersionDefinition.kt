package dev.kikugie.stonecutter.intellij.lang.access

import com.intellij.psi.PsiElement

/**
 * Groups [StitcherStringVersion][dev.kikugie.stonecutter.intellij.lang.psi.StitcherStringVersion]
 * and [StitcherSemanticVersion][dev.kikugie.stonecutter.intellij.lang.psi.StitcherSemanticVersion].
 */
sealed interface VersionDefinition : PsiElement