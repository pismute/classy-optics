package classy.optics

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

private[classy] object SumMacros:
  private def reviewOf[T: Type, A: Type](using Quotes): Expr[Review[T, A]] =
    val review: Expr[A => T] = '{ a => a.asInstanceOf[T] }
    '{ Review[T, A]($review) }

  private def mkReview[S: Type, T: Type, A: Type](using Quotes): Option[Expr[Review[S, A]]] =
    Type.of[T] match
      case '[A *: tpes] => Some(reviewOf[S, A])
      case '[tpe *: tpes] =>
        Expr.summon[Iso[tpe, A]] match
          case None => mkReview[S, tpes, A]
          case Some(iso) =>
            Some('{ ${ iso }.composeReview(${ reviewOf[S, tpe] }) })
      case _ => None
  end mkReview

  def genReview[T: Type, A: Type](using q: Quotes): Expr[Review[T, A]] =
    import quotes.reflect.*

    Expr
      .summon[Mirror.SumOf[T]]
      .map { case '{ $m: Mirror.SumOf[T] { type MirroredElemTypes = elementTypes } } =>
        mkReview[T, elementTypes, A].getOrElse(
          report.errorAndAbort(s"${Type.show[A]} cannot reach in ${Type.show[T]}")
        )
      }
      .getOrElse(report.errorAndAbort(s"${Type.show[T]} is not a sum type"))
  end genReview

  private def prismOf[T <: Matchable: Type, A: Type](using Quotes): Expr[Prism[T, A]] =
    val preview: Expr[PartialFunction[T, A]] = '{ { case a: A => a } }
    val review: Expr[A => T] = '{ a => a.asInstanceOf[T] }
    '{ Prism.fromPF[T, A]($preview)($review) }

  private def mkPrism[S <: Matchable: Type, T: Type, A: Type](using Quotes): Option[Expr[Prism[S, A]]] =
    Type.of[T] match
      case '[A *: tpes] => Some(prismOf[S, A])
      case '[tpe *: tpes] =>
        Expr.summon[Iso[tpe, A]] match
          case None => mkPrism[S, tpes, A]
          case Some(iso) =>
            Some('{ $iso.composePrism(${ prismOf[S, tpe] }) })
      case _ => None
  end mkPrism

  def genPrism[T <: Matchable: Type, A: Type](using q: Quotes): Expr[Prism[T, A]] =
    import quotes.reflect.*

    Expr
      .summon[Mirror.SumOf[T]]
      .map { case '{ $m: Mirror.SumOf[T] { type MirroredElemTypes = elementTypes } } =>
        mkPrism[T, elementTypes, A].getOrElse(
          report.errorAndAbort(s"${Type.show[A]} cannot reach in ${Type.show[T]}")
        )
      }
      .getOrElse(report.errorAndAbort(s"${Type.show[T]} is not a sum type"))
  end genPrism

end SumMacros
