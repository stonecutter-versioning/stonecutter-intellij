package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import dev.kikugie.stonecutter.intellij.lang.navigation.UserDataHolderAccessor


sealed interface PsiBlock : PsiElement, UserDataHolderAccessor {
    fun <T> accept(visitor: Visitor<T>): T

    interface Visitor<T> {
        fun visitContent(content: Content): T
        fun visitComment(comment: Comment): T
        fun visitCode(code: Code): T
        fun visitRoot(root: Root): T
    }
    
    class Content(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock {
        var firstLeaf: SmartPsiElementPointer<PsiElement>? by PSI_CONTENT_FIRST
        var lastLeaf: SmartPsiElementPointer<PsiElement>? by PSI_CONTENT_LAST

        var localStart: Int? by PSI_BLOCK_START
        var localEnd: Int? by PSI_BLOCK_END

        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitContent(this)
    }
    
    class Comment(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock {
        var hostComment: SmartPsiElementPointer<PsiComment>? by PSI_COMMENT_HOST

        var localStart: Int? by PSI_BLOCK_START
        var localEnd: Int? by PSI_BLOCK_END

        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitComment(this)
    }
    
    class Code(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock {
        var hostComment: SmartPsiElementPointer<PsiComment>? by PSI_COMMENT_HOST

        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitCode(this)
    }
    
    class Root(node: ASTNode) : ASTWrapperPsiElement(node), PsiBlock {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitRoot(this)
    }

    companion object {
        val PSI_CONTENT_FIRST: Key<SmartPsiElementPointer<PsiElement>> = Key.create("PSI_CONTENT_FIRST")
        val PSI_CONTENT_LAST: Key<SmartPsiElementPointer<PsiElement>> = Key.create("PSI_CONTENT_LAST")

        val PSI_COMMENT_HOST: Key<SmartPsiElementPointer<PsiComment>> = Key.create("PSI_COMMENT_HOST")

        val PSI_BLOCK_START: Key<Int> = Key.create("PSI_BLOCK_START")
        val PSI_BLOCK_END: Key<Int> = Key.create("PSI_BLOCK_END")
    }
}