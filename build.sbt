import scala.Ordering.Implicits.*

import org.typelevel.sbt.gha.JavaSpec.Distribution.Temurin
import org.typelevel.sbt.gha.RefPredicate

import _root_.io.github.davidgregory084.ScalacOption
import _root_.io.github.davidgregory084.ScalaVersion.*
import sbtcrossproject.CrossProject
import sbtcrossproject.CrossType
import sbtcrossproject.Platform

name := "classy-optics"

ThisBuild / organization := "io.github.pismute"
ThisBuild / organizationName := "pismute"
ThisBuild / startYear := Some(2023)
ThisBuild / licenses := Seq(License.MIT)
ThisBuild / developers := List(
  Developer(
    "pismute",
    "Changwoo Park",
    "pismute@gmail.com",
    url("https://github.com/pismute")
  )
)

val Scala3 = "3.2.2"

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
      "org.scalameta" %% "munit" % "1.0.0-M7" % Test,
      "org.scalameta" %% "munit-scalacheck" % "1.0.0-M7" % Test,
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
      "org.typelevel" %% "cats-effect" % "3.4.10",
      "org.typelevel" %% "cats-effect-testkit" % "3.4.10" % Test,
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "2.0-9366e44" % Test
    )
  )

val root = project
  .enablePlugins(NoPublishPlugin)
  .aggregate(mtl.jvm, mtl.js, mtl.native, effect.jvm, effect.js, effect.native)

// ThisBuild / tlCiMimaBinaryIssueCheck := false

// ThisBuild / tlCiDocCheck := false

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("master")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Run(
    List("sbt ci-release"),
    name = Some("Publish JARs"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}",
      "CI_SONATYPE_RELEASE" -> "; sonatypePrepare; sonatypeBundleUpload"
    )
  )
)
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec(Temurin, "11"))
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("validate"), name = Some("Build project"))
)

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
  "validate",
  Seq(
    "clean",
    "scalafmtSbtCheck",
    "test",
    "package",
    "packageSrc"
  )
)

addCommandsAlias(
  "fmt",
  Seq(
//    "scalafmtAll", // it gets fails on scala3 macro
    "scalafmtSbt"
  )
)
