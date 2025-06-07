// This is a generated file. Not intended for manual editing.
package dev.kikugie.stonecutter.intellij.lang;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import dev.kikugie.stonecutter.intellij.lang.impl.*;

public interface StitcherTokenTypes {

  IElementType ASSIGNMENT = new StitcherTokenType("ASSIGNMENT");
  IElementType BUILD_METADATA = new StitcherTokenType("BUILD_METADATA");
  IElementType CONDITION = new StitcherTokenType("CONDITION");
  IElementType CONSTANT = new StitcherTokenType("CONSTANT");
  IElementType DEPENDENCY = new StitcherTokenType("DEPENDENCY");
  IElementType EXPRESSION = new StitcherTokenType("EXPRESSION");
  IElementType PREDICATE = new StitcherTokenType("PREDICATE");
  IElementType PRE_RELEASE = new StitcherTokenType("PRE_RELEASE");
  IElementType REPLACEMENT_ID = new StitcherTokenType("REPLACEMENT_ID");
  IElementType SEMANTIC_VERSION = new StitcherTokenType("SEMANTIC_VERSION");
  IElementType STRING_VERSION = new StitcherTokenType("STRING_VERSION");
  IElementType SWAP = new StitcherTokenType("SWAP");
  IElementType SWAP_ID = new StitcherTokenType("SWAP_ID");
  IElementType VERSION_CORE = new StitcherTokenType("VERSION_CORE");

  IElementType ASSIGN = new StitcherTokenType("ASSIGN");
  IElementType BINARY = new StitcherTokenType("BINARY");
  IElementType CLOSER = new StitcherTokenType("CLOSER");
  IElementType COMPARATOR = new StitcherTokenType("COMPARATOR");
  IElementType COND_MARKER = new StitcherTokenType("COND_MARKER");
  IElementType DASH = new StitcherTokenType("DASH");
  IElementType DOT = new StitcherTokenType("DOT");
  IElementType IDENTIFIER = new StitcherTokenType("IDENTIFIER");
  IElementType LEFT_BRACE = new StitcherTokenType("LEFT_BRACE");
  IElementType LITERAL = new StitcherTokenType("LITERAL");
  IElementType NUMERIC = new StitcherTokenType("NUMERIC");
  IElementType OPENER = new StitcherTokenType("OPENER");
  IElementType PLUS = new StitcherTokenType("PLUS");
  IElementType REPL_MARKER = new StitcherTokenType("REPL_MARKER");
  IElementType RIGHT_BRACE = new StitcherTokenType("RIGHT_BRACE");
  IElementType SUGAR = new StitcherTokenType("SUGAR");
  IElementType SWAP_MARKER = new StitcherTokenType("SWAP_MARKER");
  IElementType UNARY = new StitcherTokenType("UNARY");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ASSIGNMENT) {
        return new StitcherAssignmentImpl(node);
      }
      else if (type == BUILD_METADATA) {
        return new StitcherBuildMetadataImpl(node);
      }
      else if (type == CONDITION) {
        return new StitcherConditionImpl(node);
      }
      else if (type == CONSTANT) {
        return new StitcherConstantImpl(node);
      }
      else if (type == DEPENDENCY) {
        return new StitcherDependencyImpl(node);
      }
      else if (type == EXPRESSION) {
        return new StitcherExpressionImpl(node);
      }
      else if (type == PREDICATE) {
        return new StitcherPredicateImpl(node);
      }
      else if (type == PRE_RELEASE) {
        return new StitcherPreReleaseImpl(node);
      }
      else if (type == REPLACEMENT_ID) {
        return new StitcherReplacementImpl(node);
      }
      else if (type == SEMANTIC_VERSION) {
        return new StitcherSemanticVersionImpl(node);
      }
      else if (type == STRING_VERSION) {
        return new StitcherStringVersionImpl(node);
      }
      else if (type == SWAP) {
        return new StitcherSwapImpl(node);
      }
      else if (type == SWAP_ID) {
        return new StitcherSwapIdImpl(node);
      }
      else if (type == VERSION_CORE) {
        return new StitcherVersionCoreImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
