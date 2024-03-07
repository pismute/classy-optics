import scala.Ordering.Implicits.*

import org.typelevel.sbt.gha.JavaSpec.Distribution.Temurin
import org.typelevel.sbt.gha.RefPredicate

import _root_.io.github.davidgregory084.ScalacOption
import _root_.io.github.davidgregory084.ScalaVersion.*
import sbtcrossproject.CrossProject
import sbtcrossproject.CrossType
import sbtcrossproject.Platform

name := "classy-optics"

ThisBuild / tlBaseVersion := "0.2"
ThisBuild / organization := "io.github.pismute"
ThisBuild / organizationName := "pismute"
ThisBuild / startYear := Some(2023)
ThisBuild / homepage := Some(url("https://github.com/pismute/classy-optics"))
ThisBuild / licenses := Seq(License.MIT)
ThisBuild / developers := List(
  Developer(
    "pismute",
    "Changwoo Park",
    "pismute@gmail.com",
    url("https://github.com/pismute")
  )
)

val Scala3 = "3.3.3"

ThisBuild / crossScalaVersions := Seq(Scala3)
ThisBuild / scalaVersion := Scala3 // the default Scala
ThisBuild / versionScheme := Some("early-semver")

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

def myCrossProject(name: String): CrossProject =
  CrossProject(name, file(name.replace("classy-", "")))(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Full)
    .settings(
      tpolecatScalacOptions ~= fixScalcOptions,
      tpolecatDevModeOptions ++= devScalacOptions
    )

val mtl = myCrossProject("classy-mtl")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-mtl" % "1.3.1",
      "org.scalameta" %% "munit" % "1.0.0-M8" % Test,
      "org.scalameta" %% "munit-scalacheck" % "1.0.0-M8" % Test,
      "org.typelevel" %% "cats-laws" % "2.9.0" % Test,
      "org.typelevel" %% "cats-mtl-laws" % "1.3.1" % Test,
      "org.typelevel" %% "discipline-munit" % "1.0.9" % Test,
      "org.typelevel" %% "kittens" % "3.0.0" % Test
    )
  )

val effect = myCrossProject("classy-effect")
  .dependsOn(mtl % s"$Compile->$Compile;$Test->$Test")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.1",
      "org.typelevel" %% "cats-effect-testkit" % "3.5.1" % Test,
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "2.0-9366e44" % Test
    )
  )

val root = tlCrossRootProject
  .aggregate(mtl, effect)

ThisBuild / tlCiDocCheck := false

ThisBuild / tlCiScalafmtCheck := true

ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("master")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec(Temurin, "11"))

ThisBuild / githubWorkflowTargetBranches := Seq("master")

ThisBuild / githubWorkflowTargetTags := Seq("v*")

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

def addCommandsAlias(name: String, cmds: Seq[String]) =
  addCommandAlias(name, cmds.mkString(";", ";", ""))

addCommandsAlias(
  "fmt",
  Seq(
    "scalafmtAll",
    "scalafmtSbt"
  )
)
