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

public class StitcherPredicateImpl extends ASTWrapperPsiElement implements StitcherPredicate {

  public StitcherPredicateImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StitcherVisitor visitor) {
    visitor.visitPredicate(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StitcherVisitor) accept((StitcherVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<StitcherSemanticVersion> getSemanticVersionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, StitcherSemanticVersion.class);
  }

  @Override
  @NotNull
  public List<StitcherStringVersion> getStringVersionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, StitcherStringVersion.class);
  }

}
