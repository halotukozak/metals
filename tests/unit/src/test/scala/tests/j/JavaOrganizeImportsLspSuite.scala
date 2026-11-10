package tests.j

import org.eclipse.{lsp4j => l}

/**
 * End-to-end tests for `source.organizeImports` on Java sources, which is
 * backed by the Java presentation compiler.
 */
class JavaOrganizeImportsLspSuite
    extends BaseJavaPCSuite("java-organize-imports") {

  private val path = "a/src/main/java/a/Example.java"
  private val title = "Organize imports"

  private def isOrganizeImports(action: l.CodeAction): Boolean =
    action.getTitle() == title

  private def layout(code: String): String =
    s"""|/metals.json
        |{"a": {}}
        |/$path
        |${code.replace("<<", "").replace(">>", "")}""".stripMargin

  testLSP("remove-unused-and-sort") {
    cleanWorkspace()
    for {
      _ <- initialize(
        layout(
          """|package a;
             |
             |import java.util.Map;
             |import static java.util.Collections.emptyList;
             |import java.util.List;
             |
             |public class Example {
             |  <<List<String> names = emptyList();>>
             |}
             |""".stripMargin
        )
      )
      _ <- server.didOpen(path)
      codeActions <- server.assertCodeAction(
        path,
        """|package a;
           |
           |import java.util.Map;
           |import static java.util.Collections.emptyList;
           |import java.util.List;
           |
           |public class Example {
           |  <<List<String> names = emptyList();>>
           |}
           |""".stripMargin,
        title,
        List(l.CodeActionKind.SourceOrganizeImports),
        filterAction = isOrganizeImports,
      )
      _ <- client.applyCodeAction(0, codeActions, server)
      _ <- server.didChange(path)(_ => server.bufferContents(path))
      _ <- server.didSave(path)
      _ = assertNoDiff(
        server.bufferContents(path),
        """|package a;
           |
           |import static java.util.Collections.emptyList;
           |
           |import java.util.List;
           |
           |public class Example {
           |  List<String> names = emptyList();
           |}
           |""".stripMargin,
      )
    } yield ()
  }

  // Source actions are offered only when the client asks for them.
  testLSP("not-offered-for-other-kinds") {
    cleanWorkspace()
    val code =
      """|package a;
         |
         |import java.util.Map;
         |import java.util.List;
         |
         |public class Example {
         |  <<List<String> names = null;>>
         |}
         |""".stripMargin
    for {
      _ <- initialize(layout(code))
      _ <- server.didOpen(path)
      _ <- server.assertCodeAction(
        path,
        code,
        "",
        List(l.CodeActionKind.Refactor),
        filterAction = isOrganizeImports,
      )
    } yield ()
  }

  testLSP("no-action-when-already-organized") {
    cleanWorkspace()
    val code =
      """|package a;
         |
         |import java.util.List;
         |
         |public class Example {
         |  <<List<String> names = null;>>
         |}
         |""".stripMargin
    for {
      _ <- initialize(layout(code))
      _ <- server.didOpen(path)
      _ <- server.assertCodeAction(
        path,
        code,
        "",
        List(l.CodeActionKind.SourceOrganizeImports),
        filterAction = isOrganizeImports,
      )
    } yield ()
  }
}
