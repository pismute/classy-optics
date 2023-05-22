package classy.optics

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

import Macros.*
private[classy] object SumMacros:
  private type PrismOr[T, A] = Either[String, Expr[Prism[T, A]]]
  private type ReviewOr[T, A] = Either[String, Expr[Review[T, A]]]

  private def reviewOf[T: Type, A: Type](using Quotes): Expr[Review[T, A]] =
    val review: Expr[A => T] = '{ a => a.asInstanceOf[T] }
    '{ Review[T, A]($review) }

  private def mkReview[S: Type, T: Type, A: Type](using Quotes): ReviewOr[S, A] =
    Type.of[T] match
      case '[A *: tpes] => Right(reviewOf[S, A])
      case '[tpe *: tpes] =>
        Expr.summon[Iso[tpe, A]] match
          case None => mkReview[S, tpes, A]
          case Some(iso) =>
            Right('{ ${ iso }.composeReview(${ reviewOf[S, tpe] }) })
      case _ => Left(s"${Type.show[A]} cannot reach in ${Type.show[T]}")
  end mkReview

  private def genEnumReview[T: Type, A: Type](using q: Quotes): ReviewOr[T, A] =
    import quotes.reflect.*

    Expr
      .summon[Mirror.SumOf[T]]
      .toRight(s"${Type.show[T]} is not a sum type")
      .flatMap { case '{ $m: Mirror.SumOf[T] { type MirroredElemTypes = elementTypes } } =>
        mkReview[T, elementTypes, A]
      }
  end genEnumReview

  def genReview[T: Type, A: Type](using q: Quotes): Expr[Review[T, A]] =
    import quotes.reflect.*

    assertNonIdenticalType[T, A]

    genEnumReview[T, A].fold(report.errorAndAbort, identity)
  end genReview

  private def prismOf[T: Type, A: Type](using Quotes): Expr[Prism[T, A]] =
    val preview: Expr[PartialFunction[T, A]] = '{ { case a: A => a } }
    val review: Expr[A => T] = '{ a => a.asInstanceOf[T] }
    '{ Prism.fromPF[T, A]($preview)($review) }

  private def mkPrism[S: Type, T: Type, A: Type](using Quotes): PrismOr[S, A] =
    Type.of[T] match
      case '[A *: tpes] => Right(prismOf[S, A])
      case '[tpe *: tpes] =>
        Expr.summon[Iso[tpe, A]] match
          case None => mkPrism[S, tpes, A]
          case Some(iso) =>
            Right('{ $iso.composePrism(${ prismOf[S, tpe] }) })
      case _ => Left(s"${Type.show[A]} cannot reach in ${Type.show[T]}")
  end mkPrism

  private def genEnumPrism[T: Type, A: Type](using q: Quotes): PrismOr[T, A] =
    import quotes.reflect.*

    Expr
      .summon[Mirror.SumOf[T]]
      .toRight(s"${Type.show[T]} is not a sum type")
      .flatMap { case '{ $m: Mirror.SumOf[T] { type MirroredElemTypes = elementTypes } } =>
        mkPrism[T, elementTypes, A]
      }
  end genEnumPrism

  private def genUnionPrism[T: Type, A: Type](using q: Quotes): PrismOr[T, A] =
    import quotes.reflect.*

    val tRepr = TypeRepr.of[T]
    val aRepr = TypeRepr.of[A]

    def mkUnionPrism(union: TypeRepr): PrismOr[T, A] =
      union match
        case OrType(lhs, rhs)            => mkUnionPrism(lhs).orElse(mkUnionPrism(rhs))
        case t: TypeRepr if t.=:=(aRepr) => Right(prismOf[T, A])
        case _                           => Left(s"${showTypeAlias[T]} does not contain ${Type.show[A]}")

    mkUnionPrism(tRepr.dealias)
  end genUnionPrism

  def genPrism[T: Type, A: Type](using q: Quotes): Expr[Prism[T, A]] =
    import quotes.reflect.*

    assertNonIdenticalType[T, A]

    genEnumPrism[T, A]
      .orElse(genUnionPrism[T, A])
      .fold(report.errorAndAbort, identity)
  end genPrism

end SumMacros
