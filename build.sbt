name := """bigconf"""
organization := "com.r"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.2"
val igniteVersion = "2.4.0"
val pac4jVersion = "2.2.1"
libraryDependencies ++= Seq(
  guice,
  ehcache,
  cacheApi,
  "org.projectlombok" % "lombok" % "1.16.20",
  "org.apache.ignite" % "ignite-core" % igniteVersion,
  "org.apache.ignite" % "ignite-slf4j" % igniteVersion,
  "org.pac4j" % "play-pac4j" % "4.1.1",
  "org.pac4j" % "pac4j-cas" % pac4jVersion,
  "org.pac4j" % "pac4j-http" % pac4jVersion,
  "be.objectify" % "deadbolt-java_2.12" % "2.6.0"
)