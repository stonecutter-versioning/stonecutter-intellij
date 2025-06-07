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

public class StitcherExpressionImpl extends ASTWrapperPsiElement implements StitcherExpression {

  public StitcherExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StitcherVisitor visitor) {
    visitor.visitExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StitcherVisitor) accept((StitcherVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StitcherAssignment getAssignment() {
    return findChildByClass(StitcherAssignment.class);
  }

  @Override
  @Nullable
  public StitcherConstant getConstant() {
    return findChildByClass(StitcherConstant.class);
  }

  @Override
  @NotNull
  public List<StitcherExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, StitcherExpression.class);
  }

}
