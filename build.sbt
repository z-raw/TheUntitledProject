scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

val myOrganization = "ZS"
val scala3Version = "3.3.4"

val circeVersion = "0.14.1"
val catsEffectVersion = "3.5.2"
val http4sVersion = "0.23.23"
val doobieVersion = "1.0.0-RC4"
val pureConfigVersion = "0.17.4"
val log4catsVersion = "2.6.0"
val tsecVersion = "0.5.0"
val scalaTestVersion = "3.2.17"
val scalaTestCatsEffectVersion = "1.5.0"
val testContainerVersion = "1.19.1"
val logbackVersion = "1.4.11"
val slf4jVersion = "2.0.9"
val javaMailVersion = "1.6.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "theproject",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-fs2" % circeVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
      "com.github.pureconfig" %% "pureconfig-core" % pureConfigVersion,
      "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
      "org.slf4j" % "slf4j-simple" % slf4jVersion,
      "io.github.jmcardon" %% "tsec-http4s" % tsecVersion,
      "com.sun.mail" % "javax.mail" % javaMailVersion,
      "org.typelevel" %% "log4cats-noop" % log4catsVersion % Test,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % scalaTestCatsEffectVersion % Test,
      "org.testcontainers" % "testcontainers" % testContainerVersion % Test,
      "org.testcontainers" % "postgresql" % testContainerVersion % Test,
      "ch.qos.logback" % "logback-classic" % logbackVersion % Test
    ),
    Compile / mainClass := Some("theproject.Application")
  )

lazy val stagingBuild = (project in (file("build/staging")))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    name            := "chirpster-staging-1",
    version         := "0.1.6-SNAPSHOT", // Bumping the version will trigger CD by k8s
    scalaVersion    := scala3Version,
    organization    := myOrganization,
    dockerBaseImage := "openjdk:11-jre-slim-buster",
    dockerExposedPorts ++= Seq(4041),
    Compile / mainClass         := Some("theproject.Application"),
    Compile / resourceDirectory := (( Compile / resourceDirectory).value / "staging"),
    Docker / dockerBuildCommand := {
      if (sys.props("os.arch") != "amd64") {
        // Needed for local builds on Mac arm64 M1s
        dockerExecCommand.value ++ Seq("buildx", "build", "--platform=linux/amd64", "--load") ++ dockerBuildOptions.value :+ "."
      } else dockerBuildCommand.value
    }
  )
  .dependsOn(root)

