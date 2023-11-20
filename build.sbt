import scala.collection.immutable.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "SecuritiesApi"
  )

scalacOptions ++= Seq(
  "-Yretain-trees"
)

val catsCoreVersion = "2.9.0"
val catsEffectVersion = "3.5.1"
val log4catsVersion = "2.6.0"
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsCoreVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "org.typelevel" %% "log4cats-core" % log4catsVersion,
  "org.typelevel" %% "log4cats-slf4j" % log4catsVersion
)

val slf4jVersion = "2.0.5"
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
  "org.slf4j" % "slf4j-api" % slf4jVersion
)

val pureConfigVersion = "0.17.3"
val typesafeVersion = "1.4.2"
val littleConfigVersion = "4.0.0"
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % typesafeVersion,
  "com.github.losizm" %% "little-config" % littleConfigVersion
)

val circeVersion = "0.14.5"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-refined" % circeVersion
)

val doobieVersion = "1.0.0-RC4"
libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres-circe" % doobieVersion,
  "org.tpolecat" %% "doobie-refined" % doobieVersion
)

val http4sVersion = "0.23.23"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion
)

val tapirVersion = "1.6.4"
val apiSpecVersion = "0.6.0"
libraryDependencies ++= Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-refined" % tapirVersion,
  "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml" % apiSpecVersion
)

val macwireVerison = "2.5.8"
libraryDependencies ++= Seq(
  "com.softwaremill.macwire" %% "macros" % macwireVerison % Provided
)

val flywaydbVersion = "9.16.0"
libraryDependencies ++= Seq(
  "org.flywaydb" % "flyway-core" % flywaydbVersion
)

val refinedVersion = "0.11.0"
libraryDependencies ++= Seq(
  "eu.timepit" %% "refined" % refinedVersion,
  "eu.timepit" %% "refined-cats" % refinedVersion,
  "eu.timepit" %% "refined-pureconfig" % refinedVersion
)

val scalaXmlVersion = "2.1.0"
libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion
)
//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
