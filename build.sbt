name := "CsvDownloader"

version := "0.1"

scalaVersion := "3.6.4"

libraryDependencies += "org.knowm.xchart" % "xchart" % "3.8.1"
libraryDependencies += "de.erichseifert.vectorgraphics2d" % "VectorGraphics2D" % "0.13"
libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.9.5"
libraryDependencies += "org.knowm.xchart" % "xchart" % "3.8.1"
libraryDependencies += "org.apache.poi" % "poi-ooxml" % "5.2.5"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.1" cross CrossVersion.for3Use2_13
libraryDependencies += "org.mongodb" % "mongodb-driver-sync" % "4.10.2"
libraryDependencies += "com.lihaoyi" %% "upickle" % "3.1.0"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.9.1"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.0"