package dev.kikugie.stonecutter.intellij.editor.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.StonecutterBundle.BUNDLE
import org.jetbrains.annotations.PropertyKey

fun ProblemsHolder.error(element: PsiElement, @PropertyKey(resourceBundle = BUNDLE) key: String) =
    registerProblem(element, StonecutterBundle.message(key), ProblemHighlightType.GENERIC_ERROR)

fun ProblemsHolder.error(element: PsiElement, @PropertyKey(resourceBundle = BUNDLE) key: String, vararg args: Any) =
    registerProblem(element, StonecutterBundle.message(key, *args), ProblemHighlightType.GENERIC_ERROR)