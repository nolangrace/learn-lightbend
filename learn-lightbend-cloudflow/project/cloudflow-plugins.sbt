// Resolver for the cloudflow-sbt plugin
//
//resolvers += "Akka Snapshots" at "https://repo.akka.io/snapshots/"
//
//addSbtPlugin("com.lightbend.cloudflow" % "sbt-cloudflow" % "1.3.0-M18")

resolvers += Resolver.url("Pipelines Internal", url("https://dl.bintray.com/lightbend/pipelines-internal"))(Resolver.ivyStylePatterns)
resolvers += Resolver.url("lightbend-commercial", url("https://repo.lightbend.com/commercial-releases"))(Resolver.ivyStylePatterns)
resolvers += "Akka Snapshots" at "https://repo.akka.io/snapshots/"

//addSbtPlugin("com.lightbend.pipelines" % "sbt-pipelines" % "1.1.0")
addSbtPlugin("com.lightbend.pipelines" % "sbt-pipelines" % "1.2.2")
