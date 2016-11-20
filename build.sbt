import sbt.Keys._
import sbt.Project.projectToRef

// common settings for all projects
lazy val commonSettings = Seq(
  version := "1.0.1",
  organization := "scaffvis",
  scalaVersion := Dependencies.versions.scala,
  scalacOptions ++= Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature"
  )
)

// shared project - crossProject type from which JS and JVM projects are created below
lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Dependencies.sharedDependencies.value
  )
  .jsConfigure(_ enablePlugins ScalaJSPlay) //specific to the JS project

lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM")

lazy val sharedJS = shared.js.settings(name := "sharedJS")

// configurabe elision to exclude some development code from production builds
lazy val elisionOptions = settingKey[Seq[String]]("Set limit for compiler elision")

// client project
lazy val client: Project = (project in file("client"))
  .settings(commonSettings: _*)
  .settings(
    name := "client",
    libraryDependencies ++= Dependencies.clientDependencies.value,
    libraryDependencies ++= Dependencies.clientServerDependencies.value,
    elisionOptions := Seq(), // no elision in development mode
    scalacOptions ++= elisionOptions.value,
    jsDependencies ++= Dependencies.clientJsDependencies.value, //bundled libraries
    // RuntimeDOM is needed for tests
    jsDependencies += RuntimeDOM % "test",
    // yes, we want to package JS dependencies
    skip in packageJSDependencies := false,
    // use Scala.js provided launcher code to start the client app
    persistLauncher := true,
    persistLauncher in Test := false
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .dependsOn(sharedJS)

// client projects to be included in the server build
lazy val clients = Seq(client)

// server project
lazy val server = (project in file("server"))
  .settings(commonSettings: _*)
  .settings(
    name := "server",
    libraryDependencies ++= Dependencies.serverDependencies.value,
    libraryDependencies ++= Dependencies.clientServerDependencies.value,
    // connect to the client project
    scalaJSProjects := clients,
    pipelineStages := Seq(scalaJSProd, digest, gzip)
  )
  .settings(commands ++= Seq(generateCmd, releaseCmd))
  .enablePlugins(PlayScala, LauncherJarPlugin)
  .disablePlugins(PlayLayoutPlugin) // use the standard directory layout instead of Play's custom
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJVM)
  .dependsOn(generator)

// generator project
lazy val generator = (project in file("generator"))
  .settings(commonSettings: _*)
  .settings(
    name := "generator",
    libraryDependencies ++= Dependencies.generatorDependencies.value,
    fork := true,
    baseDirectory in run := file("."), //same as non-forked
    javaOptions in run ++= Seq("-server", "-Xmx8G", "-XX:+UseConcMarkSweepGC")
  )
  .settings(commands ++= Seq(generateCmd, releaseCmd))
  .dependsOn(sharedJVM)

// define a "generate" command to build a production release
lazy val generateCmd = Command.command("generate") {
  state => "generator/run ImportPubChem" ::
    "generator/run GenerateScaffolds" ::
    "generator/run GenerateHierarchy" ::
    state
}

// define a "release" command to build a production release
lazy val releaseCmd = Command.command("release") {
  state => "set elisionOptions in client := Seq(\"-Xelide-below\", \"WARNING\")" :: //elide in production mode
    "scaffvis/clean" ::
    "scaffvis/test" ::
    "server/dist" ::
    "set elisionOptions in client := Seq()" ::
    state
}

// the root project, aggreages all subproject
lazy val scaffvis = (project in file("."))
  .settings(commonSettings: _*)
  .settings(commands ++= Seq(generateCmd, releaseCmd))
  .aggregate(sharedJS, sharedJVM, server, client, generator)

// load a selected project at startup
// onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
