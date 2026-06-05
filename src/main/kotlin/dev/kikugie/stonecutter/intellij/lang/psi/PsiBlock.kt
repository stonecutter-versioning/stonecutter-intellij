package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset

sealed interface PsiBlock {
    val startOffset: Int
    val endOffset: Int

    class Content(override val startOffset: Int, override val endOffset: Int) : PsiBlock

    class Comment(val element: PsiComment, val localStart: Int, val localEnd: Int): PsiBlock {
        override val startOffset: Int get() = element.startOffset + localStart
        override val endOffset: Int get() = element.startOffset + localEnd
    }

    class Code(val host: PsiComment, val entries: List<PsiBlock>) : PsiBlock {
        override val startOffset: Int get() = host.startOffset
        override val endOffset: Int get() = entries.lastOrNull()?.endOffset ?: host.endOffset
    }

    class Root(val entries: List<PsiBlock>) : PsiBlock {
        override val startOffset: Int get() = entries.firstOrNull()?.startOffset ?: 0
        override val endOffset: Int get() = entries.lastOrNull()?.endOffset ?: 0
    }
}