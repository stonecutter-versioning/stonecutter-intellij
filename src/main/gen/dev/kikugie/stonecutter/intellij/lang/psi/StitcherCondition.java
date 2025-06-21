// This is a generated file. Not intended for manual editing.
package dev.kikugie.stonecutter.intellij.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import dev.kikugie.stonecutter.intellij.lang.access.ScopeDefinition;
import dev.kikugie.stonecutter.intellij.lang.access.ConditionDefinition;

public interface StitcherCondition extends ScopeDefinition, ConditionDefinition {

  @Nullable
  StitcherExpression getExpression();

}
