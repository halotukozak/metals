package scala.meta.internal.metals.codelenses

import scala.annotation.unused
import scala.concurrent.Future

import scala.meta.internal.implementation.TextDocumentWithPath
import scala.meta.io.AbsolutePath

import org.eclipse.lsp4j as l

trait CodeLens {
  def isEnabled: Boolean

  def codeLenses(
      @unused textDocumentWithPath: TextDocumentWithPath
  ): Future[Seq[l.CodeLens]] =
    Future.successful(Seq.empty)

  def codeLenses(@unused path: AbsolutePath): Future[Seq[l.CodeLens]] =
    Future.successful(Seq.empty)
}
