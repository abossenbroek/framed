import sbtcrossproject.{crossProject, CrossType}
import com.typesafe.sbt.packager.docker._

lazy val commonSettings = Seq(
  scalaVersion := "2.12.6",
  organization := "com.framed"
)

lazy val server = (project in file("server")).settings(commonSettings).settings(
  name := """framed-server""",
  version := "1.0-SNAPSHOT",
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),

  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    "com.vmunier"       %% "scalajs-scripts"       % "1.1.2",
    "com.typesafe.play" %% "play-slick"            % "3.0.1",
    "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1",
    "org.postgresql"    %% "postgresql"            % "42.2.5",
    guice,
    specs2 % Test
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile),
  // Docker.io setup
  dockerCommands := Seq(
    Cmd("FROM openjdk:8u181-jdk-slim-stretch"),
    Cmd("LABEL MAINTAINER", "Anton Bossenbroek <anton.bossenbroek@me.com>"),
    Cmd("WORKDIR", "/opt/docker"),
    Cmd("ADD", "--chown=daemon:daemon opt /opt"),
    Cmd("RUN", "[\"mkdir\", \"-p\", \"/opt/docker/logs\", \"/opt/docker/config\"]"),
    Cmd("RUN", "[\"chown\", \"-R\", \"daemon:daemon\", \"/opt/docker/logs\", \"/opt/docker/config\"]"),
    Cmd("VOLUME", "[\"/opt/docker/logs\", \"/opt/docker/config\"]"),
    Cmd("USER", "daemon"),
    Cmd("CMD", s"/opt/docker/bin/${packageName.value} -Dhttp.port=$$PORT -Dconfig.file=/opt/docker/conf/heroku.conf")
  ),
//  // use ++= to merge a sequence with an existing sequence
//  dockerCommands ++= Seq(
//    ExecCmd("RUN", "mkdir", s"/opt/docker/${packageName.value}"),
//    ExecCmd("RUN", "mkdir", s"/opt/docker/${packageName.value}/run"),
//    ExecCmd("RUN", "chown", "-R", "daemon:daemon", s"/opt/docker/${packageName.value}/")
//  ),
  dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/config"),
  ).enablePlugins(PlayScala,DockerPlugin).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(commonSettings).settings(
  scalaJSUseMainModuleInitializer := true,
  version := "1.0-SNAPSHOT",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}

//
//libraryDependencies += guice
//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
//
//herokuAppName in Compile := "framed"
//
//enablePlugins(JavaAppPackaging)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.framed.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.framed.binders._"
