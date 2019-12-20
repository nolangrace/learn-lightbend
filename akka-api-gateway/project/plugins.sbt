addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "0.7.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.5.2")

credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")
resolvers += "com-mvn" at "https://repo.lightbend.com/commercial-releases/"
resolvers += Resolver.url("lightbend-commercial", url("https://repo.lightbend.com/commercial-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.lightbend.cinnamon" % "sbt-cinnamon" % "2.12.4")