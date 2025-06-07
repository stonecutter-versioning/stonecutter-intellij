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

public class StitcherAssignmentImpl extends ASTWrapperPsiElement implements StitcherAssignment {

  public StitcherAssignmentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StitcherVisitor visitor) {
    visitor.visitAssignment(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StitcherVisitor) accept((StitcherVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StitcherDependency getDependency() {
    return findChildByClass(StitcherDependency.class);
  }

  @Override
  @NotNull
  public List<StitcherPredicate> getPredicateList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, StitcherPredicate.class);
  }

  @Override
  @Nullable
  public StitcherSemanticVersion getSemanticVersion() {
    return findChildByClass(StitcherSemanticVersion.class);
  }

  @Override
  @Nullable
  public StitcherStringVersion getStringVersion() {
    return findChildByClass(StitcherStringVersion.class);
  }

}
