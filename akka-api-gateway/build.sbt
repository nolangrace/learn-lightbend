
lazy val akkaHttpVersion = "10.1.11"
lazy val akkaVersion    = "2.6.1"
lazy val akkaGrpcVersion = "0.7.3"

enablePlugins(AkkaGrpcPlugin)

version := "1.6"

// Add the Cinnamon Agent for run and test
cinnamon in run := true

lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin, JavaAppPackaging, Cinnamon)
  .settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.6"
    )),
    name := "akka-api-gateway",
    dockerBaseImage := "registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift",
    packageName in Docker := "quickstart-app",
    //version in Docker ~= (_.replace('+', '-'))
    //dynver in Docker ~= (_.replace('+', '-'))
    dockerUpdateLatest := true,
    dockerExposedPorts := Seq(8080, 8081, 9001),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "com.typesafe.akka" %% "akka-discovery"           % akkaVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.2.3",
      Cinnamon.library.cinnamonAkka,
      Cinnamon.library.cinnamonAkkaStream,
      Cinnamon.library.cinnamonAkkaHttp,
      Cinnamon.library.cinnamonPrometheus,
      Cinnamon.library.cinnamonPrometheusHttpServer,
      Cinnamon.library.cinnamonPrometheusHttpServer,

      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.0.8"         % Test
    )
  )
