libraryDependencies in ThisBuild += compilerPlugin(kindProjectorPlugin)

name := "cats-playground-scala"
// scalaVersion in ThisBuild := "2.12.8"

lazy val compilerOptions = Seq(
  "-Ypartial-unification",
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
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
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
      catsMtl,
      circe,
      circeGeneric,
      circeGenericExtras,
      circeLiteral,
      circeParser,
      circeOptics,
      circeConfig,
      fs2,
      fs2Io,
      specs2,
      logback,
      scalaOrganization.value %  "scala-reflect" % scalaVersion.value, // required for shapeless macros
      shapeless,
      monocleCore,
      monocleMacro,
      monocleLaw,
      log4catsCore,
      log4catsSlf4j,
      scalaParser,
      dispatchHttp
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
      fs2Io,
      solrJ
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

lazy val testmtl = project.
  settings(
    name := "test-mtl",
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

lazy val testhl7 = project.
  settings(
    name := "test-hl7",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      fs2,
      hapiV231,
      scalaXml,

      http4sBlazeClient,
      http4sCirce,
      kantanXpath,
      kantanXPathJava8,

      solrs
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

lazy val testfs2cassandra = project.
  settings(
    name := "test-fs2-cassandra",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      fs2,
      fs2Io,

      doobie,
      doobieH2,
      doobieScalatest,
      doobieHikari,
      doobiePostgres,
      h2,
      flyway,
      postgreSQLJdbc,

      tikaCore,
      tikaParsers,
      imageIO,
      jbig2,
      jpeg2000,

      cassandraCore,

      http4sBlazeClient,
      http4sCirce,

      commonsPool2
    ),
    dependencyOverrides ++= Seq(
      guava
    )
  ).dependsOn(
  common
)

lazy val testmonocle = project.
  settings(
    name := "test-monocle",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      
    )
  ).dependsOn(
    common
  )

val Http4sVersion = "0.21.0-M5"
val CirceVersion = "0.12.1"
val CirceGenericExVersion = "0.12.2"
val CirceOpticsVersion = "0.12.0"
val CirceConfigVersion = "0.7.0"
val Specs2Version = "4.7.0"
val LogbackVersion = "1.2.3"
val CatsVersion = "2.0.0"
val CatsMtlVersion = "0.7.0"
val DoobieVersion = "0.8.4"
val H2Version = "1.4.199"
val KindProjectorVersion = "0.10.3"
val FlywayVersion = "6.5.7"
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
val MonocleVersion = "2.0.0"
val Log4CatsVersion = "1.0.1"
val SolrJVersion = "8.4.0"
val SolrsVersion = "2.4.0"
val TikaVersion = "1.23"
val ImageIOVersion = "1.4.0"
val Jbig2Version = "3.0.3"
val Jpeg2000Version = "1.3.0"
val CassandraCoreVersion = "3.8.0"
val GuavaVersion = "19.0"
val ShapelessVersion = "2.3.3"
val PostgreSQLJdbcVersion = "42.2.9"
val CommonsPool2 = "2.8.0"

val cats = "org.typelevel" %% "cats-core" % CatsVersion
val catsMtl = "org.typelevel" %% "cats-mtl-core" % CatsMtlVersion

val circe = "io.circe" %% "circe-core" % CirceVersion
val circeGeneric = "io.circe" %% "circe-generic" % CirceVersion
val circeGenericExtras = "io.circe" %% "circe-generic-extras" % CirceGenericExVersion
val circeLiteral = "io.circe" %% "circe-literal" % CirceVersion
val circeParser = "io.circe" %% "circe-parser" % CirceVersion
val circeOptics = "io.circe" %% "circe-optics" % CirceOpticsVersion
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
val doobiePostgres = "org.tpolecat" %% "doobie-postgres"  % DoobieVersion

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

val monocleCore = "com.github.julien-truffaut" %%  "monocle-core"  % MonocleVersion
val monocleMacro = "com.github.julien-truffaut" %%  "monocle-macro" % MonocleVersion
val monocleLaw = "com.github.julien-truffaut" %%  "monocle-law"   % MonocleVersion % "test"

val log4catsCore = "io.chrisdavenport" %% "log4cats-core" % Log4CatsVersion
val log4catsSlf4j = "io.chrisdavenport" %% "log4cats-slf4j" % Log4CatsVersion

val solrJ = "org.apache.solr" % "solr-solrj" % SolrJVersion
val solrs = "io.ino" %% "solrs" % SolrsVersion

val tikaCore = "org.apache.tika" % "tika-core" % TikaVersion
val tikaParsers = "org.apache.tika" % "tika-parsers" % TikaVersion
val imageIO = "com.github.jai-imageio" % "jai-imageio-core" % ImageIOVersion
val jbig2 = "org.apache.pdfbox" % "jbig2-imageio" % Jbig2Version
val jpeg2000 = "com.github.jai-imageio" % "jai-imageio-jpeg2000" % Jpeg2000Version

val cassandraCore = "com.datastax.cassandra" % "cassandra-driver-core" % CassandraCoreVersion
val guava = "com.google.guava" % "guava" % GuavaVersion

val shapeless = "com.chuusai" %% "shapeless" % ShapelessVersion

val postgreSQLJdbc = "org.postgresql" % "postgresql" % PostgreSQLJdbcVersion

val commonsPool2 = "org.apache.commons" % "commons-pool2" % CommonsPool2


// Filter out compiler flags to make the repl experience functional...
val badConsoleFlags = Seq("-Xfatal-warnings", "-Ywarn-unused:imports")
scalacOptions in (Compile, console) ~= (_.filterNot(badConsoleFlags.contains(_)))

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := s"${name.value}-${version.value}.jar",

  assemblyMergeStrategy in assembly := {
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      val strategy = oldStrategy(x)
      if (strategy == MergeStrategy.deduplicate)
        MergeStrategy.first
      else
        strategy
  },
  test in assembly := {}
)
