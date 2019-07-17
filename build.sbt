name := "akka-tls-hangs"
version := "0.1"
scalaVersion := "2.12.8"

scalaSource in Compile := baseDirectory.value / "src"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "com.typesafe.akka" %% "akka-http" % "10.1.8",
  "com.typesafe.akka" %% "akka-http2-support" % "10.1.8"
)