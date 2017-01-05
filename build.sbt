name := "twicsy-downloader"

version := "1.0"

lazy val `twicsy-downloader` = (project in file("."))

scalaVersion := "2.11.8"

libraryDependencies += "org.jsoup" % "jsoup" % "1.10.2"

unmanagedResourceDirectories in Test +=  baseDirectory ( _ /"target/web/public/test" ).value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

scalaSource in Compile := baseDirectory.value / "src"

assemblyJarName in assembly := "twicsy-downloader.jar"