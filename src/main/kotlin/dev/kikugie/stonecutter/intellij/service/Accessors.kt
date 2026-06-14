package dev.kikugie.stonecutter.intellij.service

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.service.model.GradleIdentityPath.Companion.findIdentityPath
import dev.kikugie.stonecutter.intellij.service.model.SCProjectNode
import dev.kikugie.stonecutter.intellij.service.model.SCProjectParameters

val PsiElement.stonecutterService: StonecutterService
    inline get() = project.stonecutterService

val PsiElement.stonecutterNode: SCProjectNode?
    get() = findIdentityPath()?.let { stonecutterService.lookup.nodes[it] }

val PsiElement.stonecutterParameters: SCProjectParameters?
    inline get() = stonecutterNode?.parameters

val Project.stonecutterService: StonecutterService
    get() = getService(StonecutterService::class.java)