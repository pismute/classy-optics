import _root_.io.github.davidgregory084.ScalaVersion.*
import _root_.io.github.davidgregory084.ScalacOption

import scala.Ordering.Implicits._

name := "classy-optics"

ThisBuild / tlBaseVersion := "0.1"

ThisBuild / organization := "pismute"
ThisBuild / organizationName := "pismute"
ThisBuild / startYear := Some(2023)
ThisBuild / licenses := Seq(License.MIT)

val Scala3 = "3.2.2"

ThisBuild / crossScalaVersions := Seq(Scala3)
ThisBuild / scalaVersion := Scala3 // the default Scala

def fixScalcOptions(opts: Set[ScalacOption]): Set[ScalacOption] = {
  opts.map { opt =>
    if (opt != ScalacOptions.privateKindProjector) opt
    else ScalacOptions.privateOption("kind-projector:underscores", _ >= V3_0_0)
  }
}

val devScalacOptions = Set(
  ScalacOptions.advancedOption("check-macros", _ >= V3_0_0)
)

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

val mtl = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .settings(
    name := "classy-mtl",
    tpolecatScalacOptions ~= fixScalcOptions,
    tpolecatDevModeOptions ++= devScalacOptions,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-mtl" % "1.3.1",
      "org.scalameta" %% "munit" % "1.0.0-M7" % Test,
      "org.scalameta" %% "munit-scalacheck" % "1.0.0-M7" % Test,
      "org.typelevel" %% "cats-laws" % "2.9.0" % Test,
      "org.typelevel" %% "cats-mtl-laws" % "1.3.1" % Test,
      "org.typelevel" %% "discipline-munit" % "1.0.9" % Test,
      "org.typelevel" %% "kittens" % "3.0.0" % Test
    )
  )

val effect = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .dependsOn(mtl % s"$Compile->$Compile;$Test->$Test")
  .settings(
    name := "classy-effect",
    tpolecatScalacOptions ~= fixScalcOptions,
    tpolecatDevModeOptions ++= devScalacOptions,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.4.10",
      "org.typelevel" %% "cats-effect-testkit" % "3.4.10" % Test,
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "2.0-9366e44" % Test
    )
  )

val root = tlCrossRootProject
  .aggregate(mtl, effect)

ThisBuild / tlCiMimaBinaryIssueCheck := false

ThisBuild / tlCiScalafmtCheck := true

ThisBuild / tlCiDocCheck := false

ThisBuild / githubWorkflowPublishTargetBranches := Seq() // disable publish

ThisBuild / githubWorkflowIncludeClean := false // publish disabled, clean is unnecessary

ThisBuild / githubWorkflowTargetBranches := Seq("master")

val scalaSteward = "pismute-steward[bot]"

ThisBuild / mergifyStewardConfig := Some(
  MergifyStewardConfig(
    author = scalaSteward,
    mergeMinors = true,
    action = MergifyAction.Merge(Some("squash"))
  )
)

ThisBuild / mergifyPrRules += {
  val authorCondition = MergifyCondition.Custom("author=pismute-steward[bot]")
  MergifyPrRule(
    "label scala-steward's PRs",
    List(authorCondition),
    List(MergifyAction.Label(List("dependency-update")))
  )
}
