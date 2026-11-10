package scala.meta.internal.jpc

import scala.jdk.CollectionConverters._

import scala.meta.pc.VirtualFileParams

import com.sun.source.tree.ImportTree
import com.sun.source.util.Trees
import org.eclipse.{lsp4j => l}

/**
 * Removes unused imports and sorts the rest the same way Google Java Format
 * does: static imports first, then a blank line, then the other imports, each
 * block in alphabetical order. The rest of the file is not touched.
 *
 * If there is anything but whitespace between the imports (for example a
 * comment), the imports are not reordered, because that would drop the
 * comment. The unused ones are still removed.
 */
final class JavaOrganizeImportsProvider(
    compiler: JavaMetalsCompiler,
    params: VirtualFileParams
) {

  private case class Span(tree: ImportTree, start: Int, end: Int) {
    def name: String = tree.getQualifiedIdentifier.toString
  }

  def textEdits(): List[l.TextEdit] = {
    params.checkCanceled()
    val compile = compiler.compilationTask(params).withAnalyzePhase()
    params.checkCanceled()
    val cu = compile.cu
    val positions = Trees.instance(compile.task).getSourcePositions
    val text = cu.getSourceFile.getCharContent(true).toString
    val spans = cu.getImports.asScala.toList.map { tree =>
      Span(
        tree,
        positions.getStartPosition(cu, tree).toInt,
        positions.getEndPosition(cu, tree).toInt
      )
    }
    // Synthetic imports have no position, we can't edit them.
    if (spans.isEmpty || spans.exists(s => s.start < 0 || s.end < 0)) Nil
    else {
      val unused =
        new UnusedImportDiagnosticProvider(compile).unusedImports().toSet
      val kept = spans.filterNot(s => unused.contains(s.tree))
      val toRange = (start: Int, end: Int) =>
        Positions.toLspRange(cu.getLineMap, start, end, text)

      if (hasTextBetween(spans, text)) {
        spans
          .filter(s => unused.contains(s.tree))
          .map { s =>
            val (start, end) = lineToDelete(s, text)
            new l.TextEdit(toRange(start, end), "")
          }
      } else {
        val first = spans.head.start
        val last = spans.last.end
        val region = text.substring(first, last)
        if (kept.isEmpty) {
          // Also drop the blank lines that followed the imports.
          val end = text.indexWhere(!_.isWhitespace, last) match {
            case -1 => text.length
            case index => index
          }
          List(new l.TextEdit(toRange(first, end), ""))
        } else {
          val organized = render(kept, text)
          if (organized == region) Nil
          else List(new l.TextEdit(toRange(first, last), organized))
        }
      }
    }
  }

  private def render(kept: List[Span], text: String): String = {
    val eol = if (text.contains("\r\n")) "\r\n" else "\n"
    val (statics, regular) = kept.partition(_.tree.isStatic)
    List(statics, regular)
      .map(_.sortBy(_.name).map(s => text.substring(s.start, s.end)))
      .filter(_.nonEmpty)
      .map(_.mkString(eol))
      .mkString(eol + eol)
  }

  private def hasTextBetween(spans: List[Span], text: String): Boolean =
    spans.zip(spans.tail).exists { case (previous, next) =>
      text.substring(previous.end, next.start).exists(!_.isWhitespace)
    }

  /**
   * The whole line when the import is alone on it, so that no blank line is
   * left behind, otherwise only the import.
   */
  private def lineToDelete(span: Span, text: String): (Int, Int) = {
    val lineStart = text.lastIndexOf('\n', span.start - 1) + 1
    val lineEnd = text.indexOf('\n', span.end) match {
      case -1 => text.length
      case index => index + 1
    }
    val isAlone =
      text.substring(lineStart, span.start).trim.isEmpty &&
        text.substring(span.end, lineEnd).trim.isEmpty
    if (isAlone) (lineStart, lineEnd) else (span.start, span.end)
  }
}
