package dev.kikugie.stonecutter.intellij.impl

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiNameIdentifierOwner
import dev.kikugie.stonecutter.intellij.lang.StitcherFile

class StitcherCompletion : CompletionContributor() {

}

interface StitcherReference : PsiNameIdentifierOwner
enum class StitcherReferenceType {
    SWAP, CONST, DEPENDENCY,
}
class StitcherReferenceImpl(val type: StitcherReferenceType, node: ASTNode) : ASTWrapperPsiElement(node), StitcherReference {
    override fun setName(name: String): PsiElement {
        TODO("Not yet implemented")
    }

    override fun getNameIdentifier(): PsiElement? {
        TODO("Not yet implemented")
    }
}

object StitcherReferenceFactory {
    fun property(project: Project, name: String): StitcherReference = file(project, name).firstChild as StitcherReference
    fun file(project: Project, text: String): StitcherFile = PsiFileFactory.getInstance(project)
        .createFileFromText("dummy.stitcher", StitcherFile.Type, text) as StitcherFile
}