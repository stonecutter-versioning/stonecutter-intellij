// This is a generated file. Not intended for manual editing.
package dev.kikugie.stonecutter.intellij.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StitcherPredicate extends PsiElement {

  @NotNull
  List<StitcherSemanticVersion> getSemanticVersionList();

  @NotNull
  List<StitcherStringVersion> getStringVersionList();

}
