import Dependencies._
import org.typelevel.scalacoptions.ScalacOptions

ThisBuild / version := "0.2.0"
ThisBuild / organization := "io.elijahlamoon"

Global / onLoad := {
  (Global / onLoad).value andThen ("dependencyUpdates" :: _)
}

lazy val baseSettings = Seq(
  scalaVersion := "2.13.12",
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  tpolecatScalacOptions ++= Set(
    ScalacOptions.deprecation,
    ScalacOptions.lintAdaptedArgs,
    ScalacOptions.lintConstant,
    ScalacOptions.lintInferAny,
    ScalacOptions.lintMissingInterpolator,
    ScalacOptions.lintPrivateShadow,
    ScalacOptions.lintTypeParameterShadow,
    ScalacOptions.lintDeprecation,
    ScalacOptions.lintImplicitNotFound,
    ScalacOptions.lintInaccessible,
    ScalacOptions.warnDeadCode,
    ScalacOptions.warnValueDiscard
  ) ++ ScalacOptions.warnUnusedOptions,
  tpolecatExcludeOptions ++= ScalacOptions.fatalWarningOptions, // i dont know why, but warnings are fatal by default
  console / tpolecatExcludeOptions ++= ScalacOptions.defaultConsoleExclude
)

lazy val sharedDependencies = Seq(
  cats,
  catsEffect,
  fs2,
  quillJdbc,
  quillDoobie,
  sqliteJdbc,
  doobieCore,
  doobieHikari,
  logstage,
  logstageSlf4j,
  fly4s,
  chimney,
  munit % Test
)

lazy val root = project
  .in(file("."))
  .settings(baseSettings)
  .settings(name := "dott-root", publishArtifact := false)
  .aggregate(simple, overengineered)

lazy val simple = project
  .in(file("simple"))
  .settings(baseSettings)
  .settings(name := "dott-simple", libraryDependencies += munit % Test)

lazy val overengineered = project
  .in(file("overengineered"))
  .settings(baseSettings)
  .settings(
    name := "dott-overengineered",
    libraryDependencies ++= sharedDependencies
  )

addCommandAlias("run", "simple/run")
