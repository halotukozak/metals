package tests.pc

import java.net.URI
import java.util.Optional

import scala.meta.internal.jdk.CollectionConverters._
import scala.meta.internal.metals.CompilerOffsetParams
import scala.meta.internal.metals.EmptyCancelToken
import scala.meta.internal.metals.TextEdits
import scala.meta.pc.CodeActionId

import munit.Location
import munit.TestOptions

abstract class BaseJavaOrganizeImportsSuite extends BaseJavaPCSuite {

  def check(
      name: TestOptions,
      original: String,
      expected: String,
      filename: String = "A.java",
  )(implicit location: Location): Unit =
    test(name) {
      val params = CompilerOffsetParams(
        URI.create(s"file:///$filename"),
        original,
        0,
        EmptyCancelToken,
      )
      val edits = presentationCompiler
        .codeAction(
          params,
          CodeActionId.OrganizeImports,
          Optional.empty[Object](),
        )
        .get()
        .asScala
        .toList
      assertNoDiff(TextEdits.applyEdits(original, edits), expected)
    }
}
