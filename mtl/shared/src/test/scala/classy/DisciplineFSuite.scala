package classy

import cats.Functor
import cats.syntax.functor.*

import org.typelevel.discipline.Laws

import munit.{DisciplineSuite, Location, TestOptions}

trait DisciplineFSuite extends DisciplineSuite:
  final def checkAllF[F[_]: Functor](name: String, ruleSet: F[Laws#RuleSet])(using
      loc: Location
  ): Unit = checkAllF(new TestOptions(name, Set.empty, loc), ruleSet)

  final def checkAllF[F[_]: Functor](options: TestOptions, ruleSet: F[Laws#RuleSet])(using
      loc: Location
  ): Unit =
    test(options)(ruleSet.map(checkRuleSet(options)))

  private def checkRuleSet(options: TestOptions)(ruleSet: Laws#RuleSet): Unit =
    ruleSet.all.properties.toList.foreach { case (id, prop) =>
      property(options.withName(s"${options.name}: $id")) {
        prop
      }
    }

end DisciplineFSuite
