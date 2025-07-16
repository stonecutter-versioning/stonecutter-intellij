package dev.kikugie.stonecutter.intellij.service

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.model.SCProjectNode

val PsiElement.stonecutterService: StonecutterService
    get() = project.stonecutterService

val PsiElement.stonecutterNode: SCProjectNode?
    get() = stonecutterService.lookup.node(this)

val Project.stonecutterService: StonecutterService
    get() = getService(StonecutterService::class.java)