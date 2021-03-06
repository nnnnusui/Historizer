name := "Historizer"

version := "0.1"

scalaVersion := "2.13.6"

val CalibanVersion       = "1.1.1"
val AkkaHttpCirceVersion = "1.35.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka"     %% "akka-actor-typed"  % "2.6.15",
  "com.github.ghostdogpr" %% "caliban"           % CalibanVersion,
  "com.github.ghostdogpr" %% "caliban-akka-http" % CalibanVersion,
  "de.heikoseeberger"     %% "akka-http-circe"   % "1.38.2",
  "ch.megard"             %% "akka-http-cors"    % "1.1.2",
  "com.typesafe.slick"    %% "slick"             % "3.3.3",
  "com.h2database"         % "h2"                % "1.4.200"
)

enablePlugins(CalibanPlugin)
