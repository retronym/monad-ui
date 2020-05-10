scalaVersion := "2.12.12-bin-a6cc4ba-SNAPSHOT"

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

name := "monad-ui"

scalacOptions += "-Xasync"

resolvers += "pr-scala snapshots" at "https://scala-ci.typesafe.com/artifactory/scala-pr-validation-snapshots/"

libraryDependencies += "io.monix" %% "monix" % "3.2.1"

libraryDependencies += "org.typelevel" %% "cats-effect" % "2.1.3"