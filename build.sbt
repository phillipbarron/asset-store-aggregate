import Path.rebase

val Organization = "bbc"
val Name = "asset-store-aggregate"
val Version: String = Option(System.getenv("BUILD_VERSION")) getOrElse "DEV"
val ScalatraVersion = "2.6.4"
val JettyVersion = "9.4.14.v20181114"
val Json4sVersion = "3.6.3"
val slickVersion = "3.3.2"

val JettyPort: Int = sys.props.get("jetty.port") map (_.toInt) getOrElse 8080

// To connect to BBC repos (https://github.com/sbt/sbt/issues/4629)
updateOptions := updateOptions.value.withGigahorse(false)

// Project settings
organization := Organization
name := Name
version := Version
scalaVersion := "2.11.12"
scalacOptions ++= Seq("-feature", "-language:implicitConversions", "-deprecation")

libraryDependencies ++= Seq(
  // core
  "bbc.shared" %% "cam-scalatra-chassis" % "2.0.1",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.580",
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.0.4",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.8.0",

  // test
  "bbc.shared" %% "cam-scalatra-test-chassis" % "2.0.1" % "test",
  "org.apache.kafka" % "kafka-clients" % "2.2.1"
)

Compile / resourceGenerators += task {
  val fromBase = sourceDirectory.value / "main" / "webapp"
  val toBase = resourceManaged.value / "main" / "webapp"
  for {
    (from, to) <- fromBase ** "*" pair rebase(fromBase, toBase)
  } yield {
    Sync.copy(from, to)
    to
  }
}

Compile / unmanagedResourceDirectories += baseDirectory(_ / "src/main/webapp").value

// Assembly settings
assembly / assemblyJarName := s"$Name.jar"
assembly / mainClass := Some("Launcher")
assembly / sbt.Keys.test := {}
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "io.netty.versions.properties", xs @ _*) => MergeStrategy.discard
  case "mime.types" => MergeStrategy.filterDistinctLines
  case x =>
    val oldStrategy = ( assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

fork in Test := true

//Cucumber Settings
enablePlugins(CucumberPlugin)
CucumberPlugin.monochrome := false
CucumberPlugin.features := List("cucumber")
CucumberPlugin.glues := List("steps")

enablePlugins(ScalatraPlugin)

resolvers ++= Seq(
  Resolver.file("Local Ivy repository", file(Path.userHome + "/.ivy2/local")),
  "BBC Forge Maven Releases" at "https://dev.bbc.co.uk/maven2/releases/",
  "BBC Forge Maven Snapshots" at "https://dev.bbc.co.uk/maven2/snapshots",
  "BBC Forge Artifactory" at "https://dev.bbc.co.uk/artifactory/repo",
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
)
