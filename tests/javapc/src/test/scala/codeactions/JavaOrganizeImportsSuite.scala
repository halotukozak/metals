package codeactions

import tests.pc.BaseJavaOrganizeImportsSuite

class JavaOrganizeImportsSuite extends BaseJavaOrganizeImportsSuite {

  check(
    "sort-static-first",
    """|package a;
       |
       |import java.util.List;
       |import static java.util.Collections.emptyList;
       |import java.util.ArrayList;
       |import static java.lang.Math.max;
       |
       |class A {
       |  List<String> a = new ArrayList<>();
       |  List<String> b = emptyList();
       |  int c = max(1, 2);
       |}
       |""".stripMargin,
    """|package a;
       |
       |import static java.lang.Math.max;
       |import static java.util.Collections.emptyList;
       |
       |import java.util.ArrayList;
       |import java.util.List;
       |
       |class A {
       |  List<String> a = new ArrayList<>();
       |  List<String> b = emptyList();
       |  int c = max(1, 2);
       |}
       |""".stripMargin,
  )

  check(
    "remove-unused",
    """|package a;
       |
       |import java.util.Map;
       |import java.util.List;
       |import java.util.Set;
       |
       |class A {
       |  List<String> a;
       |}
       |""".stripMargin,
    """|package a;
       |
       |import java.util.List;
       |
       |class A {
       |  List<String> a;
       |}
       |""".stripMargin,
  )

  check(
    "remove-all",
    """|package a;
       |
       |import java.util.Map;
       |import java.util.Set;
       |
       |class A {}
       |""".stripMargin,
    """|package a;
       |
       |class A {}
       |""".stripMargin,
  )

  check(
    "remove-duplicate",
    """|package a;
       |
       |import java.util.List;
       |import java.util.List;
       |
       |class A {
       |  List<String> a;
       |}
       |""".stripMargin,
    """|package a;
       |
       |import java.util.List;
       |
       |class A {
       |  List<String> a;
       |}
       |""".stripMargin,
  )

  // Both imports have the same name, but they import different things.
  check(
    "static-and-regular-import-of-the-same-name",
    """|package a;
       |
       |import java.util.Collections;
       |import static java.util.Collections.*;
       |import java.util.List;
       |
       |class A {
       |  List<String> a = emptyList();
       |  void f(List<String> l) { Collections.sort(l); }
       |}
       |""".stripMargin,
    """|package a;
       |
       |import static java.util.Collections.*;
       |
       |import java.util.Collections;
       |import java.util.List;
       |
       |class A {
       |  List<String> a = emptyList();
       |  void f(List<String> l) { Collections.sort(l); }
       |}
       |""".stripMargin,
  )

  check(
    "remove-redundant",
    """|package a;
       |
       |import java.lang.String;
       |import java.util.List;
       |
       |class A {
       |  List<String> a;
       |}
       |""".stripMargin,
    """|package a;
       |
       |import java.util.List;
       |
       |class A {
       |  List<String> a;
       |}
       |""".stripMargin,
  )

  check(
    "wildcard",
    """|package a;
       |
       |import java.util.List;
       |import java.util.*;
       |import java.io.*;
       |
       |class A {
       |  List<String> a;
       |  Map<String, String> b;
       |}
       |""".stripMargin,
    """|package a;
       |
       |import java.util.*;
       |import java.util.List;
       |
       |class A {
       |  List<String> a;
       |  Map<String, String> b;
       |}
       |""".stripMargin,
  )

  check(
    "unresolved-import-is-kept",
    """|package a;
       |
       |import java.util.List;
       |import com.missing.Foo;
       |import java.util.Map;
       |
       |class A {
       |  List<String> a;
       |}
       |""".stripMargin,
    """|package a;
       |
       |import com.missing.Foo;
       |import java.util.List;
       |
       |class A {
       |  List<String> a;
       |}
       |""".stripMargin,
  )

  check(
    "already-organized",
    """|package a;
       |
       |import static java.lang.Math.max;
       |
       |import java.util.List;
       |
       |class A {
       |  List<String> a;
       |  int b = max(1, 2);
       |}
       |""".stripMargin,
    """|package a;
       |
       |import static java.lang.Math.max;
       |
       |import java.util.List;
       |
       |class A {
       |  List<String> a;
       |  int b = max(1, 2);
       |}
       |""".stripMargin,
  )

  check(
    "no-imports",
    """|package a;
       |
       |class A {}
       |""".stripMargin,
    """|package a;
       |
       |class A {}
       |""".stripMargin,
  )

  check(
    "comment-between-imports-only-removes-unused",
    """|package a;
       |
       |import java.util.Set;
       |// keep the order: List must come first
       |import java.util.List;
       |import java.util.ArrayList;
       |import java.util.Map;
       |
       |class A {
       |  List<String> a = new ArrayList<>();
       |}
       |""".stripMargin,
    """|package a;
       |
       |// keep the order: List must come first
       |import java.util.List;
       |import java.util.ArrayList;
       |
       |class A {
       |  List<String> a = new ArrayList<>();
       |}
       |""".stripMargin,
  )

  check(
    "javadoc-reference-keeps-import",
    """|package a;
       |
       |import java.util.Map;
       |import java.util.List;
       |
       |/** See {@link Map}. */
       |class A {
       |  List<String> a;
       |}
       |""".stripMargin,
    """|package a;
       |
       |import java.util.List;
       |import java.util.Map;
       |
       |/** See {@link Map}. */
       |class A {
       |  List<String> a;
       |}
       |""".stripMargin,
  )
}
