package dev.kikugie.stonecutter.intellij.lang.util

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.nullableLazyValueUnsafe
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.descendantsOfType
import com.intellij.psi.util.elementType
import dev.kikugie.commons.takeAsOrNull
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.access.OpenerType
import dev.kikugie.stonecutter.intellij.lang.access.ScopeDefinition
import org.antlr.intellij.adaptor.lexer.RuleIElementType
import org.antlr.intellij.adaptor.lexer.TokenIElementType
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private val SCOPE_DEF: Key<SmartPsiElementPointer<ScopeDefinition>> = Key.create("STITCHER_DEFINITION")

val PsiElement.antlrType: Int
    get() = elementType
        ?.takeAsOrNull<TokenIElementType>()
        ?.takeIf { it.language == StitcherLang }
        ?.antlrTokenType
        ?: -1

val PsiElement.antlrRule: Int
    get() = elementType
        ?.takeAsOrNull<RuleIElementType>()
        ?.takeIf { it.language == StitcherLang }
        ?.ruleIndex
        ?: -1

val PsiFile.isInjected: Boolean
    get() = InjectedLanguageManager.getInstance(project).isInjectedFragment(this)

/**Injected Stitcher file instance, or `null` if it doesn't exist.*/
val PsiComment.stitcherFile: StitcherFile?
    get() = InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(this)
        ?.firstNotNullOfOrNull { it.first as? StitcherFile }

/**The host [PsiComment] for the injected file.*/
val StitcherFile.containingComment: PsiComment
    get() = FileContextUtil.getFileContext(this) as PsiComment

/**Cached injected [ScopeDefinition] for this comment.*/
val PsiComment.commentDefinition: SmartPsiElementPointer<ScopeDefinition>?
    get() = nullableLazyValueUnsafe(SCOPE_DEF) {
        val file = stitcherFile ?: return@nullableLazyValueUnsafe null
        val def = file.descendantsOfType<ScopeDefinition>().firstOrNull()
            ?: return@nullableLazyValueUnsafe null
        SmartPointerManager.getInstance(project).createSmartPsiElementPointer(def, file)
    }

val PsiElement?.openerType: OpenerType
    get() = when (this?.text ?: "\u0000") {
        "{" -> OpenerType.OPEN
        ">>" -> OpenerType.WORD
        "\u0000" -> OpenerType.LINE
        else -> error("Element is not a scope opener")
    }

internal fun <T : PsiElement> element(key: Key<SmartPsiElementPointer<T>>, getter: () -> T?) : ReadOnlyProperty<PsiElement, T?> =
    SmartElementPointerProperty(key, getter)

internal fun <T> cached(key: Key<CachedValue<T>>, getter: () -> T) : ReadOnlyProperty<PsiElement, T> =
    SmartPointerProperty(key, getter)

private class SmartPointerProperty<T>(val key: Key<CachedValue<T>>, val getter: () -> T) : ReadOnlyProperty<PsiElement, T> {
    override fun getValue(thisRef: PsiElement, property: KProperty<*>): T = thisRef.cachedValue()

    private fun PsiElement.cachedValue() = CachedValuesManager.getCachedValue(this, key) {
        CachedValueProvider.Result.create(getter(), this)
    }
}

private class SmartElementPointerProperty<T : PsiElement>(val key: Key<SmartPsiElementPointer<T>>, val getter: () -> T?) : ReadOnlyProperty<PsiElement, T?> {
    override fun getValue(thisRef: PsiElement, property: KProperty<*>): T? = thisRef.elementPointer()?.element

    private fun PsiElement.elementPointer() = nullableLazyValueUnsafe(key) {
        getter()?.let { SmartPointerManager.getInstance(project).createSmartPsiElementPointer(it) }
    }
}