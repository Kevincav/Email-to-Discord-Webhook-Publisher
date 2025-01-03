lazy val root = (project in file("."))
  .settings(
    name := "DiscordEmailWebhook",
    scalaVersion := "3.6.2",
    version := "v1.0.0",
    libraryDependencies ++= Seq(
      // AWS
      "com.amazonaws" % "aws-java-sdk-s3" % "1.12.780",
      "com.amazonaws" % "aws-lambda-java-events" % "3.14.0",

      // Email Parsing and Conversion
      "javax.mail" % "mail" % "1.4.7",
      "org.apache.commons" % "commons-email" % "1.6.0",
      "io.github.furstenheim" % "copy_down" % "1.1",
      "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.24.3",

      // Http Requests for Webhook Publish
      "com.typesafe.play" %% "play-json" % "2.10.6",
      "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M20",
      "com.softwaremill.sttp.client4" %% "async-http-client-backend" % "4.0.0-M20",

      // Testing
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalatestplus" %% "mockito-5-12" % "3.2.19.0" % Test,
      "com.github.tomakehurst" % "wiremock-standalone" % "3.0.1",
    ),
    assemblyMergeStrategy := (_ => MergeStrategy.first)
  )
