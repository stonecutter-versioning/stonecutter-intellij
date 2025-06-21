// This is a generated file. Not intended for manual editing.
package dev.kikugie.stonecutter.intellij.lang.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import dev.kikugie.stonecutter.intellij.lang.access.ScopeDefinition;
import dev.kikugie.stonecutter.intellij.lang.access.ConditionDefinition;

public class StitcherVisitor extends PsiElementVisitor {

  public void visitAssignment(@NotNull StitcherAssignment o) {
    visitPsiElement(o);
  }

  public void visitBuildMetadata(@NotNull StitcherBuildMetadata o) {
    visitPsiElement(o);
  }

  public void visitCondition(@NotNull StitcherCondition o) {
    visitScopeDefinition(o);
    // visitConditionDefinition(o);
  }

  public void visitConstant(@NotNull StitcherConstant o) {
    visitPsiElement(o);
  }

  public void visitDependency(@NotNull StitcherDependency o) {
    visitPsiElement(o);
  }

  public void visitExpression(@NotNull StitcherExpression o) {
    visitPsiElement(o);
  }

  public void visitPreRelease(@NotNull StitcherPreRelease o) {
    visitPsiElement(o);
  }

  public void visitPredicate(@NotNull StitcherPredicate o) {
    visitPsiElement(o);
  }

  public void visitReplacement(@NotNull StitcherReplacement o) {
    visitScopeDefinition(o);
  }

  public void visitSemanticVersion(@NotNull StitcherSemanticVersion o) {
    visitPsiElement(o);
  }

  public void visitStringVersion(@NotNull StitcherStringVersion o) {
    visitPsiElement(o);
  }

  public void visitSwap(@NotNull StitcherSwap o) {
    visitScopeDefinition(o);
  }

  public void visitSwapId(@NotNull StitcherSwapId o) {
    visitPsiElement(o);
  }

  public void visitVersionCore(@NotNull StitcherVersionCore o) {
    visitPsiElement(o);
  }

  public void visitScopeDefinition(@NotNull ScopeDefinition o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
