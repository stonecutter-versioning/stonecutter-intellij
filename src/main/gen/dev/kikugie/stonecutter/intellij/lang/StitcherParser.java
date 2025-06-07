// This is a generated file. Not intended for manual editing.
package dev.kikugie.stonecutter.intellij.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class StitcherParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    var tree = b.getTreeBuilt();
    return tree;
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return definition(b, l + 1);
  }

  /* ********************************************************** */
  // explicit_predicate | implicit_predicate
  static boolean any_predicate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "any_predicate")) return false;
    boolean r;
    r = explicit_predicate(b, l + 1);
    if (!r) r = implicit_predicate(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // dependency ASSIGN predicate
  //     | combined_predicate
  public static boolean assignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignment")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT, "<assignment>");
    r = assignment_0(b, l + 1);
    if (!r) r = combined_predicate(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // dependency ASSIGN predicate
  private static boolean assignment_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignment_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = dependency(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    r = r && predicate(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // simple_expression BINARY expression
  static boolean binary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binary_expression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simple_expression(b, l + 1);
    r = r && consumeToken(b, BINARY);
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER (DOT IDENTIFIER)*
  public static boolean build_metadata(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "build_metadata")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && build_metadata_1(b, l + 1);
    exit_section_(b, m, BUILD_METADATA, r);
    return r;
  }

  // (DOT IDENTIFIER)*
  private static boolean build_metadata_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "build_metadata_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!build_metadata_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "build_metadata_1", c)) break;
    }
    return true;
  }

  // DOT IDENTIFIER
  private static boolean build_metadata_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "build_metadata_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // explicit_predicate predicate*
  static boolean combined_predicate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "combined_predicate")) return false;
    if (!nextTokenIs(b, "", COMPARATOR, NUMERIC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = explicit_predicate(b, l + 1);
    r = r && combined_predicate_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // predicate*
  private static boolean combined_predicate_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "combined_predicate_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!predicate(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "combined_predicate_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // CLOSER? SUGAR* expression? OPENER?
  public static boolean condition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONDITION, "<condition>");
    r = condition_0(b, l + 1);
    r = r && condition_1(b, l + 1);
    r = r && condition_2(b, l + 1);
    r = r && condition_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CLOSER?
  private static boolean condition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_0")) return false;
    consumeToken(b, CLOSER);
    return true;
  }

  // SUGAR*
  private static boolean condition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SUGAR)) break;
      if (!empty_element_parsed_guard_(b, "condition_1", c)) break;
    }
    return true;
  }

  // expression?
  private static boolean condition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_2")) return false;
    expression(b, l + 1);
    return true;
  }

  // OPENER?
  private static boolean condition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_3")) return false;
    consumeToken(b, OPENER);
    return true;
  }

  /* ********************************************************** */
  // LITERAL
  public static boolean constant(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constant")) return false;
    if (!nextTokenIs(b, LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LITERAL);
    exit_section_(b, m, CONSTANT, r);
    return r;
  }

  /* ********************************************************** */
  // COND_MARKER condition
  //     | SWAP_MARKER swap
  //     | REPL_MARKER replacement
  static boolean definition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "definition")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = definition_0(b, l + 1);
    if (!r) r = definition_1(b, l + 1);
    if (!r) r = definition_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COND_MARKER condition
  private static boolean definition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "definition_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COND_MARKER);
    r = r && condition(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SWAP_MARKER swap
  private static boolean definition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "definition_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SWAP_MARKER);
    r = r && swap(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // REPL_MARKER replacement
  private static boolean definition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "definition_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REPL_MARKER);
    r = r && replacement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LITERAL
  public static boolean dependency(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dependency")) return false;
    if (!nextTokenIs(b, LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LITERAL);
    exit_section_(b, m, DEPENDENCY, r);
    return r;
  }

  /* ********************************************************** */
  // COMPARATOR (string_version | semantic_version) | semantic_version
  static boolean explicit_predicate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "explicit_predicate")) return false;
    if (!nextTokenIs(b, "", COMPARATOR, NUMERIC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = explicit_predicate_0(b, l + 1);
    if (!r) r = semantic_version(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMPARATOR (string_version | semantic_version)
  private static boolean explicit_predicate_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "explicit_predicate_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMPARATOR);
    r = r && explicit_predicate_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // string_version | semantic_version
  private static boolean explicit_predicate_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "explicit_predicate_0_1")) return false;
    boolean r;
    r = string_version(b, l + 1);
    if (!r) r = semantic_version(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // binary_expression
  //     | simple_expression
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, EXPRESSION, "<expression>");
    r = binary_expression(b, l + 1);
    if (!r) r = simple_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE expression RIGHT_BRACE
  static boolean group_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_expression")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // string_version | semantic_version
  static boolean implicit_predicate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "implicit_predicate")) return false;
    if (!nextTokenIs(b, "", LITERAL, NUMERIC)) return false;
    boolean r;
    r = string_version(b, l + 1);
    if (!r) r = semantic_version(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER (DOT IDENTIFIER)*
  public static boolean pre_release(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pre_release")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && pre_release_1(b, l + 1);
    exit_section_(b, m, PRE_RELEASE, r);
    return r;
  }

  // (DOT IDENTIFIER)*
  private static boolean pre_release_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pre_release_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!pre_release_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "pre_release_1", c)) break;
    }
    return true;
  }

  // DOT IDENTIFIER
  private static boolean pre_release_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pre_release_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // any_predicate+
  public static boolean predicate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "predicate")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PREDICATE, "<predicate>");
    r = any_predicate(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!any_predicate(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "predicate", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LITERAL
  public static boolean replacement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "replacement")) return false;
    if (!nextTokenIs(b, LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LITERAL);
    exit_section_(b, m, REPLACEMENT_ID, r);
    return r;
  }

  /* ********************************************************** */
  // version_core (DASH pre_release)? (PLUS build_metadata)?
  public static boolean semantic_version(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "semantic_version")) return false;
    if (!nextTokenIs(b, NUMERIC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = version_core(b, l + 1);
    r = r && semantic_version_1(b, l + 1);
    r = r && semantic_version_2(b, l + 1);
    exit_section_(b, m, SEMANTIC_VERSION, r);
    return r;
  }

  // (DASH pre_release)?
  private static boolean semantic_version_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "semantic_version_1")) return false;
    semantic_version_1_0(b, l + 1);
    return true;
  }

  // DASH pre_release
  private static boolean semantic_version_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "semantic_version_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DASH);
    r = r && pre_release(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (PLUS build_metadata)?
  private static boolean semantic_version_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "semantic_version_2")) return false;
    semantic_version_2_0(b, l + 1);
    return true;
  }

  // PLUS build_metadata
  private static boolean semantic_version_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "semantic_version_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PLUS);
    r = r && build_metadata(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // unary_expression
  //     | group_expression
  //     | assignment
  //     | constant
  static boolean simple_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simple_expression")) return false;
    boolean r;
    r = unary_expression(b, l + 1);
    if (!r) r = group_expression(b, l + 1);
    if (!r) r = assignment(b, l + 1);
    if (!r) r = constant(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // LITERAL
  public static boolean string_version(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_version")) return false;
    if (!nextTokenIs(b, LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LITERAL);
    exit_section_(b, m, STRING_VERSION, r);
    return r;
  }

  /* ********************************************************** */
  // CLOSER | (swap_id OPENER?)
  public static boolean swap(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "swap")) return false;
    if (!nextTokenIs(b, "<swap>", CLOSER, LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SWAP, "<swap>");
    r = consumeToken(b, CLOSER);
    if (!r) r = swap_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // swap_id OPENER?
  private static boolean swap_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "swap_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = swap_id(b, l + 1);
    r = r && swap_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // OPENER?
  private static boolean swap_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "swap_1_1")) return false;
    consumeToken(b, OPENER);
    return true;
  }

  /* ********************************************************** */
  // LITERAL
  public static boolean swap_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "swap_id")) return false;
    if (!nextTokenIs(b, LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LITERAL);
    exit_section_(b, m, SWAP_ID, r);
    return r;
  }

  /* ********************************************************** */
  // UNARY expression
  static boolean unary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_expression")) return false;
    if (!nextTokenIs(b, UNARY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, UNARY);
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NUMERIC (DOT NUMERIC)*
  public static boolean version_core(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_core")) return false;
    if (!nextTokenIs(b, NUMERIC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NUMERIC);
    r = r && version_core_1(b, l + 1);
    exit_section_(b, m, VERSION_CORE, r);
    return r;
  }

  // (DOT NUMERIC)*
  private static boolean version_core_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_core_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!version_core_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "version_core_1", c)) break;
    }
    return true;
  }

  // DOT NUMERIC
  private static boolean version_core_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_core_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, NUMERIC);
    exit_section_(b, m, null, r);
    return r;
  }

}
