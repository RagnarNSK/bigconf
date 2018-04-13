name := """bigconf"""
organization := "com.r"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += "org.projectlombok" % "lombok" % "1.16.20"

val igniteVersion = "2.1.0"
libraryDependencies += "org.apache.ignite" % "ignite-core" % igniteVersion
libraryDependencies += "org.apache.ignite" % "ignite-slf4j" % igniteVersion
