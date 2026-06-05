package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import dev.kikugie.stonecutter.intellij.lang.navigation.UserDataHolderAccessor


sealed interface PsiBlock : PsiElement, UserDataHolderAccessor {
    class Content(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock {
        var firstLeaf: SmartPsiElementPointer<PsiElement>? by PSI_CONTENT_FIRST
        var lastLeaf: SmartPsiElementPointer<PsiElement>? by PSI_CONTENT_LAST

        var localStart: Int? by PSI_BLOCK_START
        var localEnd: Int? by PSI_BLOCK_END
    }
    class Comment(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock {
        var hostComment: SmartPsiElementPointer<PsiComment>? by PSI_COMMENT_HOST

        var localStart: Int? by PSI_BLOCK_START
        var localEnd: Int? by PSI_BLOCK_END
    }
    class Code(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock {
        var hostComment: SmartPsiElementPointer<PsiComment>? by PSI_COMMENT_HOST
    }
    class Root(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock

    companion object {
        val PSI_CONTENT_FIRST: Key<SmartPsiElementPointer<PsiElement>> = Key.create("PSI_CONTENT_FIRST")
        val PSI_CONTENT_LAST: Key<SmartPsiElementPointer<PsiElement>> = Key.create("PSI_CONTENT_LAST")

        val PSI_COMMENT_HOST: Key<SmartPsiElementPointer<PsiComment>> = Key.create("PSI_COMMENT_HOST")

        val PSI_BLOCK_START: Key<Int> = Key.create("PSI_BLOCK_START")
        val PSI_BLOCK_END: Key<Int> = Key.create("PSI_BLOCK_END")
    }
}