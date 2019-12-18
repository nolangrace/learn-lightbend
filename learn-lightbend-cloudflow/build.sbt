import sbt._
import sbt.Keys._
import scalariform.formatter.preferences._

lazy val root =
  Project(id = "root", base = file("."))
    .settings(
      name := "root",
      skip in publish := true,
    )
    .withId("root")
    .settings(commonSettings)
    .aggregate(
      learnlightbendcloudflow,
      datamodel,
      akkaStreams,
      sparkAggregation
    )

//tag::docs-CloudflowApplicationPlugin-example[]
lazy val learnlightbendcloudflow = appModule("learn-lightbend-cloudflow")
  .enablePlugins(CloudflowApplicationPlugin)
  .settings(commonSettings)
  .settings(
    name := "learn-lightbend-cloudflow"
  )
  .dependsOn(akkaStreams, sparkAggregation)
//end::docs-CloudflowApplicationPlugin-example[]

lazy val datamodel = appModule("datamodel")
  .enablePlugins(CloudflowLibraryPlugin)

lazy val akkaStreams= appModule("akka-streams")
    .enablePlugins(CloudflowAkkaStreamsLibraryPlugin)
    .settings(
      commonSettings,
      libraryDependencies ++= Seq(
        "com.typesafe.akka"         %% "akka-http-spray-json"   % "10.1.10",
        "ch.qos.logback"            %  "logback-classic"        % "1.2.3",
        "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "1.1.2",
        "com.github.nosan" % "embedded-cassandra" % "3.0.1",
        "org.scalatest"             %% "scalatest"              % "3.0.8"    % "test"
      )
    )
  .dependsOn(datamodel)

lazy val sparkAggregation = appModule("spark-aggregation")
    .enablePlugins(CloudflowSparkLibraryPlugin)
    .settings(
      commonSettings,
      Test / parallelExecution := false,
      Test / fork := true,
      libraryDependencies ++= Seq(
	      "ch.qos.logback" %  "logback-classic"    % "1.2.3",
        "org.scalatest"  %% "scalatest"          % "3.0.8"  % "test"
      )
    )
  .dependsOn(datamodel)

def appModule(moduleID: String): Project = {
  Project(id = moduleID, base = file(moduleID))
    .settings(
      name := moduleID
    )
    .withId(moduleID)
    .settings(commonSettings)
}

lazy val commonSettings = Seq(
  scalaVersion := "2.12.10",
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked"
  ),

  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused-import"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,

  scalariformPreferences := scalariformPreferences.value
    .setPreference(AlignParameters, false)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 90)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(DoubleIndentMethodDeclaration, true)
    .setPreference(RewriteArrowSymbols, true)
    .setPreference(DanglingCloseParenthesis, Preserve)
    .setPreference(NewlineAtEndOfFile, true)
    .setPreference(AllowParamGroupsOnNewlines, true)
)
