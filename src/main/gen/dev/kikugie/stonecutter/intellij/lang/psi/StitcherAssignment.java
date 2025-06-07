// This is a generated file. Not intended for manual editing.
package dev.kikugie.stonecutter.intellij.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StitcherAssignment extends PsiElement {

  @Nullable
  StitcherDependency getDependency();

  @NotNull
  List<StitcherPredicate> getPredicateList();

  @Nullable
  StitcherSemanticVersion getSemanticVersion();

  @Nullable
  StitcherStringVersion getStringVersion();

}
