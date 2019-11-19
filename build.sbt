libraryDependencies in ThisBuild += compilerPlugin(kindProjectorPlugin)

name := "cats-playground-scala"
// scalaVersion in ThisBuild := "2.12.8"

lazy val compilerOptions = Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:_"
)

lazy val commonSettings = Seq(
  organization := "com.eztier",
  version := "0.1.1-SNAPSHOT",
  scalaVersion := "2.12.8",
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("public"),
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val settings = commonSettings

lazy val common = project
  .settings(
    name := "common",
    settings,
    libraryDependencies ++= Seq(
      cats,
      circe,
      circeGeneric,
      circeGenericExtras,
      circeLiteral,
      circeParser,
      circeConfig,
      specs2,
      logback,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
  )
  
lazy val testhttp4sdoobie = project.
  settings(
    name := "test-http4s-doobie",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      http4sBlazeServer,
      http4sBlazeClient,
      http4sCirce,
      http4sDsl,
      doobie,
      doobieH2,
      doobieScalatest,
      doobieHikari,
      h2,
      flyway,
      specs2
    )
  ).dependsOn(
    common
  )

lazy val testfs2pubsub = project.
  settings(
    name := "test-fs2-pubsub",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      fs2,
      fs2Io
    )
  ).dependsOn(
  common
)

lazy val testhttp4sclient = project.
  settings(
    name := "test-http4s-client",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      http4sBlazeClient,
      http4sCirce,
      fs2,
      fs2Io
    )
  ).dependsOn(
  common
)

lazy val testdoobiefs2 = project.
  settings(
    name := "test-doobie-fs2",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      doobie,
      doobieH2,
      doobieScalatest,
      doobieHikari,
      h2,
      flyway,
      fs2,
      fs2Io
    )
  ).dependsOn(
  common
)

lazy val testxmlfs2 = project.
  settings(
    name := "test-xml-fs2",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      http4sBlazeClient,
      http4sCirce,
      fs2,
      fs2Io,

      kantanXpath,
      kantanXpathCats,
      kantanXPathJava8,
      xstream,

      doobie,
      doobieH2,
      doobieScalatest,
      doobieHikari,
      h2,
      flyway,

      xs4s,

      mssqlJdbc
    )
  ).dependsOn(
  common
)

lazy val testbadsqlmodel = project.
  settings(
    name := "test-bad-sql-model",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      fs2,
      fs2Io,
      kantanXpath,
      kantanXpathCats,
      xstream,

      doobie,
      doobieH2,
      doobieScalatest,
      doobieHikari,
      h2,
      flyway
    )
  ).dependsOn(
  common
)

lazy val clickmock = project.
  settings(
    name := "clickmock",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      scalaXml,
      scalaParser,
      dispatchHttp,
      fs2,

      specs2,
      scalaTest,
      http4sBlazeServer,
      http4sDsl,
      http4sTesting,

      doobie,
      doobieH2,
      doobieScalatest,
      doobieHikari,
      h2,
      mssqlJdbc
    )
  ).dependsOn(
    common
  )

val Http4sVersion = "0.21.0-M5"
val CirceVersion = "0.12.1"
val CirceGenericExVersion = "0.12.2"
val CirceConfigVersion = "0.7.0"
val Specs2Version = "4.7.0"
val LogbackVersion = "1.2.3"
val CatsVersion = "2.0.0"
val DoobieVersion = "0.8.4"
val H2Version = "1.4.199"
val KindProjectorVersion = "0.10.3"
val FlywayVersion = "6.0.4"
val FS2Version = "2.0.0"
val KantanXPathVersion = "0.5.0"
val XStreamVersion = "1.4.11.1"
val Xs4sVersion = "0.4"
val MssqlJdbcVersion = "7.4.1.jre8"
val HapiVersion = "2.3"
val DispatchHttpVerison = "0.14.1"
val ScalaXmlVersion = "1.0.6"
val ScalaParserVersion = "1.0.6"
val ScalaTestVersion = "3.2.0-M1"

val cats = "org.typelevel" %% "cats-core" % CatsVersion

val circe = "io.circe" %% "circe-core" % CirceVersion
val circeGeneric = "io.circe" %% "circe-generic" % CirceVersion
val circeGenericExtras = "io.circe" %% "circe-generic-extras" % CirceGenericExVersion
val circeLiteral = "io.circe" %% "circe-literal" % CirceVersion
val circeParser = "io.circe" %% "circe-parser" % CirceVersion
val circeConfig = "io.circe" %% "circe-config" % CirceConfigVersion

val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % Http4sVersion
val http4sBlazeClient = "org.http4s" %% "http4s-blaze-client" % Http4sVersion
val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion
val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion
val http4sTesting = "org.http4s" %% "http4s-testing" % Http4sVersion % Test


val doobie = "org.tpolecat" %% "doobie-core" % DoobieVersion
val doobieH2 ="org.tpolecat" %% "doobie-h2" % DoobieVersion
val doobieScalatest ="org.tpolecat" %% "doobie-scalatest" % DoobieVersion
val doobieHikari ="org.tpolecat" %% "doobie-hikari" % DoobieVersion

val h2 = "com.h2database" % "h2" % H2Version

val flyway = "org.flywaydb" % "flyway-core" % FlywayVersion

val specs2 = "org.specs2"      %% "specs2-core"         % Specs2Version % "test"
val logback = "ch.qos.logback" % "logback-classic" % LogbackVersion

val kindProjectorPlugin = ("org.typelevel" %% "kind-projector" % KindProjectorVersion).cross(CrossVersion.binary)

val fs2 = "co.fs2" %% "fs2-core" % FS2Version
val fs2Io = "co.fs2" %% "fs2-io" % FS2Version

val kantanXpath = "com.nrinaudo" %% "kantan.xpath" % KantanXPathVersion
val kantanXpathCats = "com.nrinaudo" %% "kantan.xpath-cats" % KantanXPathVersion
val kantanXPathJava8 = "com.nrinaudo" %% "kantan.xpath-java8" % KantanXPathVersion

val xstream = "com.thoughtworks.xstream" % "xstream" % XStreamVersion

val xs4s = "com.scalawilliam" %% "xs4s" % Xs4sVersion

val mssqlJdbc = "com.microsoft.sqlserver" % "mssql-jdbc" % MssqlJdbcVersion

val hapiV231 = "ca.uhn.hapi" % "hapi-structures-v231" % HapiVersion

val dispatchHttp = "org.dispatchhttp" %% "dispatch-core" % DispatchHttpVerison
val scalaXml = "org.scala-lang.modules" %% "scala-xml" % ScalaXmlVersion
val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % ScalaParserVersion

val scalaTest = "org.scalatest" %% "scalatest" % ScalaTestVersion % Test

// Filter out compiler flags to make the repl experience functional...
val badConsoleFlags = Seq("-Xfatal-warnings", "-Ywarn-unused:imports")
scalacOptions in (Compile, console) ~= (_.filterNot(badConsoleFlags.contains(_)))

// Skip tests for assembly  
lazy val assemblySettings = Seq(
  assemblyJarName in assembly := s"${name.value}-${version.value}.jar",
  
  assemblyMergeStrategy in assembly := {
    case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
    case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
    case "application.conf"                            => MergeStrategy.concat
    case "logback.xml"                            => MergeStrategy.first
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
  test in assembly := {}
)
