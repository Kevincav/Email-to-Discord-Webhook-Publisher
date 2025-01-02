lazy val root = (project in file("."))
  .settings(
    name := "DiscordEmailWebhook",
    scalaVersion := "2.13.15",
    version := "v1.0.0",
    libraryDependencies ++= Seq(
      // AWS 1.x
      "com.amazonaws" % "aws-java-sdk-s3" % "1.12.780",
      "com.amazonaws" % "aws-lambda-java-events" % "3.14.0",

      // MIME Email Parser
      "javax.mail" % "mail" % "1.4.7" excludeAll(
        ExclusionRule(organization = "com.sun.mail")),
      "org.apache.commons" % "commons-email" % "1.6.0",

      // Html to Markdown
      "io.github.furstenheim" % "copy_down" % "1.1",

      // Webhook Publisher
      "club.minnced" % "discord-webhooks" % "0.8.4",

      // Log4j
      "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.24.3"
    ),
    assemblyMergeStrategy := (_ => MergeStrategy.first)
  )
