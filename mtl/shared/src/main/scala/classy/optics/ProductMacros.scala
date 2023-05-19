package classy.optics

import Macros.*

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

private[classy] object ProductMacros:
  private def indexOf[T: Type, A: Type](using Quotes): Int =
    indexOf0[T, A](0)

  private def indexOf0[T: Type, A: Type](acc: Int)(using Quotes): Int =
    Type.of[T] match
      case '[EmptyTuple]  => -1
      case '[A *: tpes]   => acc
      case '[tpe *: tpes] => indexOf0[tpes, A](acc + 1)

  private def showCaseClass[T: Type](using q: Quotes): String =
    import quotes.reflect.*

    val s = TypeRepr.of[T].typeSymbol
    val fields = s.caseFields.map(_.tree).map { case ValDef(name, tpt, _) =>
      s"$name: ${tpt.show}"
    }
    s"case class ${s.name}(${fields.mkString(", ")})"

  private def checkAmbiguityOfProduct[T: Type](using q: Quotes): Unit =
    import quotes.reflect.*

    val typeSymbol = TypeTree.of[T].symbol

    val fieldTypes = typeSymbol.caseFields.map(_.tree).map { case ValDef(name, tpt, _) => tpt.show }
    fieldTypes.groupBy(identity).find { case (_, xs) => xs.size > 1 } match
      case Some((typeS, _)) => report.errorAndAbort(s"${showCaseClass[T]} has multiple types of $typeS")
      case None             => ()

  def genGetter[T <: Product: Type, A: Type](using q: Quotes): Expr[Getter[T, A]] =
    import quotes.reflect.*

    assertNonIdenticalType[T, A]

    Expr
      .summon[Mirror.ProductOf[T]]
      .map { case '{ $m: Mirror.ProductOf[T] { type MirroredElemTypes = elementTypes } } =>
        checkAmbiguityOfProduct[T]

        val i = indexOf[elementTypes, A]

        if i < 0 then report.errorAndAbort(s"${showCaseClass[T]} has no the field of ${Type.show[A]}")
        else
          val ii: Expr[Int] = Expr(i)
          val view: Expr[T => A] = '{ t => t.productElement($ii).asInstanceOf[A] }
          '{ Getter[T, A]($view) }
      }
      .getOrElse(report.errorAndAbort(s"${Type.show[T]} is not a product type"))

  def genIso[T <: Product: Type, A: Type](using q: Quotes): Expr[Iso[T, A]] =
    import quotes.reflect.*

    assertNonIdenticalType[T, A]

    Expr
      .summon[Mirror.ProductOf[T]]
      .flatMap { case '{ $m: Mirror.ProductOf[T] { type MirroredElemTypes = elementTypes } } =>
        Expr
          .summon[Tuple1[A] =:= elementTypes]
          .map { _ =>
            val view: Expr[T => A] = '{ t => t.productElement(0).asInstanceOf[A] }
            val review: Expr[A => T] = '{ a => $m.fromTuple(Tuple1(a).asInstanceOf[elementTypes]) }
            '{ Iso[T, A]($view)($review) }
          }
          .orElse(report.errorAndAbort(s"${showCaseClass[T]} must have a field of ${Type.show[A]}"))
      }
      .getOrElse(report.errorAndAbort(s"${Type.show[T]} is not a case class"))

  end genIso

  def genLens[T <: Product: Type, A: Type](using q: Quotes): Expr[Lens[T, A]] =
    import quotes.reflect.*

    assertNonIdenticalType[T, A]

    Expr
      .summon[Mirror.ProductOf[T]]
      .map { case '{ $m: Mirror.ProductOf[T] { type MirroredElemTypes = elementTypes } } =>
        checkAmbiguityOfProduct[T]

        val i = indexOf[elementTypes, A]

        if i < 0 then report.errorAndAbort(s"${showCaseClass[T]} has no the field of ${Type.show[A]}")
        else
          val ii: Expr[Int] = Expr(i)
          val view: Expr[T => A] = '{ t => t.productElement($ii).asInstanceOf[A] }
          val set: Expr[T => A => T] = '{ t => a =>
            val arr = Tuple.fromProduct(t).toArray
            arr($ii) = a.asInstanceOf[Object]
            $m.fromTuple(Tuple.fromArray(arr).asInstanceOf[elementTypes])
          }
          '{ Lens[T, A]($view)($set) }
      }
      .getOrElse(report.errorAndAbort(s"${Type.show[T]} is not a product type"))

  end genLens

end ProductMacros
