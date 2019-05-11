lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion    = "2.6.0-M1"
val logbackVersion = "1.1.3"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.7"
    )),
    name := "profferz-backend",
    libraryDependencies ++= Seq(
      "org.slf4j"         % "slf4j-api"              % "1.7.7",
      "ch.qos.logback"    % "logback-core"           % logbackVersion,
      "ch.qos.logback"    % "logback-classic"        % logbackVersion,
      "com.typesafe.akka" %% "akka-http"              % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"   % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"          % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"            % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence"       % akkaVersion,
//      "org.iq80.leveldb"  % "leveldb"                % "0.7",
//      "org.fusesource.leveldbjni" % "leveldbjni-all"  % "1.8",

      "com.geteventstore" %% "akka-persistence-eventstore" % "6.0.0",

    "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test
    )
  )
