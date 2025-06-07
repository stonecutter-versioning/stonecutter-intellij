// This is a generated file. Not intended for manual editing.
package dev.kikugie.stonecutter.intellij.lang.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import dev.kikugie.stonecutter.intellij.lang.psi.*;

public class StitcherConditionImpl extends ASTWrapperPsiElement implements StitcherCondition {

  public StitcherConditionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StitcherVisitor visitor) {
    visitor.visitCondition(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StitcherVisitor) accept((StitcherVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StitcherExpression getExpression() {
    return findChildByClass(StitcherExpression.class);
  }

}
