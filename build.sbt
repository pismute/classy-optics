import scala.Ordering.Implicits.*

import org.typelevel.sbt.gha.JavaSpec.Distribution.Temurin
import org.typelevel.sbt.gha.RefPredicate
import org.typelevel.scalacoptions.*
import org.typelevel.scalacoptions.ScalaVersion.*

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

val Scala3 = "3.6.3"

ThisBuild / scalaVersion := Scala3 // the default Scala
ThisBuild / versionScheme := Some("early-semver")

val devScalacOptions = Set(
  ScalacOptions.advancedOption("check-macros", _ >= V3_0_0)
)

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

def myCrossProject(name: String): CrossProject =
  CrossProject(name, file(name.replace("classy-", "")))(JVMPlatform, JSPlatform)
    .crossType(CrossType.Full)
    .settings(
      tpolecatDevModeOptions ++= devScalacOptions
    )

val mtl = myCrossProject("classy-mtl")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-mtl" % "1.5.0",
      "org.scalameta" %% "munit" % "1.0.4" % Test,
      "org.scalameta" %% "munit-scalacheck" % "1.0.0" % Test,
      "org.typelevel" %% "cats-laws" % "2.13.0" % Test,
      "org.typelevel" %% "cats-mtl-laws" % "1.5.0" % Test,
      "org.typelevel" %% "discipline-munit" % "2.0.0" % Test,
      "org.typelevel" %% "kittens" % "3.4.0" % Test
    )
  )

val effect = myCrossProject("classy-effect")
  .dependsOn(mtl % s"$Compile->$Compile;$Test->$Test")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "org.typelevel" %% "cats-effect-testkit" % "3.5.7" % Test,
      "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "2.0.0-M2" % Test
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
