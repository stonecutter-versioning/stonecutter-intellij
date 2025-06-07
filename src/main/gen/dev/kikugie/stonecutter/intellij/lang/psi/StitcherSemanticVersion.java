// This is a generated file. Not intended for manual editing.
package dev.kikugie.stonecutter.intellij.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StitcherSemanticVersion extends PsiElement {

  @Nullable
  StitcherBuildMetadata getBuildMetadata();

  @Nullable
  StitcherPreRelease getPreRelease();

  @NotNull
  StitcherVersionCore getVersionCore();

}
