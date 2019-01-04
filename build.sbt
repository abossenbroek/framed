import sbtcrossproject.{crossProject, CrossType}

enablePlugins(DockerPlugin)

lazy val commonSettings = Seq(
  scalaVersion := "2.12.5",
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
    "com.vmunier" %% "scalajs-scripts" % "1.1.2",
    guice,
    specs2 % Test
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile)
).enablePlugins(PlayScala).
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

// Docker.io setup
// setting a maintainer which is used for all packaging types
maintainer := "Anton Bossenbroek"

// exposing the play ports (Change these to whatever you are using)
dockerExposedPorts in Docker := Seq(9000, 9443)
dockerBaseImage := "dockerfile/java:oracle-java8"
javaOptions in Universal ++= Seq(
  // JVM memory tuning
  "-J-Xmx1024m",
  "-J-Xms128m",
  // Since play uses separate pidfile we have to provide it with a proper path
  // name of the pid file must be play.pid
  s"-Dpidfile.path=/opt/docker/${packageName.value}/run/play.pid"
)


// use ++= to merge a sequence with an existing sequence
dockerCommands ++= Seq(
  ExecCmd("RUN", "mkdir", s"/opt/docker/${packageName.value}"),
  ExecCmd("RUN", "mkdir", s"/opt/docker/${packageName.value}/run"),
  ExecCmd("RUN", "chown", "-R", "daemon:daemon", s"/opt/docker/${packageName.value}/")
)



//scalaVersion := "2.12.7"
//
//libraryDependencies += guice
//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
//
//herokuAppName in Compile := "framed"
//
//enablePlugins(JavaAppPackaging)
//
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.framed.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.framed.binders._"
