import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt._

/**
  * Define all library dependencies.
  */
object Dependencies {

  // dependency versions are defined here
  object versions {
    val scala = "2.11.8"

    val scalaDom = "0.9.0"
    val scalajsReact = "0.11.1"
    val scalaCSS = "0.4.1"
    val scalajsJQuery = "0.9.0"
    val autowire = "0.2.5"
    val uPickle = "0.4.1"
    val booPickle = "1.2.4"
    val diode = "1.0.0"

    val react = "15.0.1"
    val jQuery = "2.1.4"
    val bootstrap = "3.3.6"

    val playScripts = "0.5.0"

    val betterFiles = "2.15.0"
    val mapDB = "2.0-beta13"
    val scalatest = "3.0.0-RC4"
    val scalaARM = "1.4"
  }

  // dependencies of the generator project
  val generatorDependencies = Def.setting(Seq(
    "org.scala-lang" % "scala-reflect" % versions.scala,
    "com.github.pathikrit" %% "better-files" % versions.betterFiles,
    "org.mapdb" % "mapdb" % versions.mapDB,
    "com.jsuereth" %% "scala-arm" % versions.scalaARM,
    "org.scalatest" %% "scalatest" % versions.scalatest % "test"
  ))

  // dependencies shared by client and server projects
  val clientServerDependencies = Def.setting(Seq(
    "com.lihaoyi" %%% "autowire" % versions.autowire,
    "com.lihaoyi" %%% "upickle" % versions.uPickle
  ))

  // dependencies for the shared project
  val sharedDependencies = Def.setting(Seq(
    "org.scalatest" %% "scalatest" % versions.scalatest % "test"
  ))

  // dependencies for the server project
  val serverDependencies = Def.setting(Seq(
    "com.vmunier" %% "play-scalajs-scripts" % versions.playScripts,
    "org.webjars.bower" % "bootstrap-sass" % versions.bootstrap
  ))

  // dependencies for the client project
  val clientDependencies = Def.setting(Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % versions.scalajsReact,
    "com.github.japgolly.scalajs-react" %%% "extra" % versions.scalajsReact,
    "com.github.japgolly.scalacss" %%% "ext-react" % versions.scalaCSS,
    "be.doeraene" %%% "scalajs-jquery" % versions.scalajsJQuery,
    "me.chrons" %%% "diode" % versions.diode,
    "me.chrons" %%% "diode-react" % versions.diode,
    "me.chrons" %%% "boopickle" % versions.booPickle,
    "org.scala-js" %%% "scalajs-dom" % versions.scalaDom,

    //a jsDependency but we need to exclude it's dependency on jQuery here
    "org.webjars.bower" % "bootstrap-sass" % versions.bootstrap exclude("org.webjars.bower","jquery")
  ))

  //external JS libraries into a single .js file
  val clientJsDependencies = Def.setting(Seq(
    "org.webjars.bower" % "react" % versions.react / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
    "org.webjars.bower" % "react" % versions.react / "react-dom.js" minified "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM",
    "org.webjars" % "jquery" % versions.jQuery / "jquery.js" minified "jquery.min.js",
    "org.webjars.bower" % "bootstrap-sass" % versions.bootstrap / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js"
  ))
}
