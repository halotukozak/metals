package scala.meta.internal.metals.codeactions

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import scala.meta.internal.metals.Compilers
import scala.meta.internal.metals.MetalsEnrichments._
import scala.meta.pc.CancelToken
import scala.meta.pc.CodeActionId

import org.eclipse.{lsp4j => l}

/**
 * `source.organizeImports` for Java: removes unused imports and sorts the rest
 * without formatting the file. Computed by the Java presentation compiler.
 */
class JavaSourceOrganizeImports(compilers: Compilers) extends CodeAction {

  override def kind: String = l.CodeActionKind.SourceOrganizeImports
  override def isScala: Boolean = false
  override def isJava: Boolean = true
  override def maybeCodeActionId: Option[String] =
    Some(CodeActionId.OrganizeImports)

  override def contribute(
      params: l.CodeActionParams,
      token: CancelToken,
  )(implicit ec: ExecutionContext): Future[Seq[l.CodeAction]] =
    if (!isRequested(params)) Future.successful(Nil)
    else {
      val path = params.getTextDocument.getUri.toAbsolutePath
      // The edit covers the whole import section, the position doesn't matter.
      val position = new l.TextDocumentPositionParams(
        params.getTextDocument,
        new l.Position(0, 0),
      )
      compilers
        .codeAction(position, token, CodeActionId.OrganizeImports, None)
        .map { edits =>
          if (edits.isEmpty) Nil
          else
            Seq(
              CodeActionBuilder.build(
                JavaSourceOrganizeImports.title,
                kind,
                changes = Seq(path -> edits.asScala.toSeq),
              )
            )
        }
    }

  /**
   * Source actions are only offered when the client asks for them, for example
   * `source.organizeImports` on save or `source` from the Source Action menu.
   */
  private def isRequested(params: l.CodeActionParams): Boolean =
    Option(params.getContext.getOnly)
      .exists(_.asScala.exists(requested => kind.startsWith(requested)))
}

object JavaSourceOrganizeImports {
  private final val title = "Organize imports"
}
