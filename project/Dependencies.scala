import sbt._

object Dependencies {

  object V { // versions
    val cats = "2.10.0"
    val catsEffect = "3.5.3"
    val fs2 = "3.9.4"
    val quill = "4.8.1"
    val fly4s = "1.0.0"
    val sqliteJdbc = "3.45.0.0"
    val doobie = "1.0.0-RC4"
    val izumi = "1.2.5"
    val chimney = "0.8.5"
    val munit = "1.0.0-M10"
  }

  object O { // organizations
    val typelevel = "org.typelevel"
    val fs2 = "co.fs2"
    val izumi = "io.7mind.izumi"
    val quill = "io.getquill"
    val geirolz = "com.github.geirolz"
    val xerial = "org.xerial"
    val tpolecat = "org.tpolecat"
    val scalaland = "io.scalaland"
    val scalameta = "org.scalameta"
  }

  // ----------------------------
  lazy val cats = O.typelevel %% "cats-core" % V.cats
  lazy val catsEffect = O.typelevel %% "cats-effect" % V.catsEffect
  lazy val fs2 = O.fs2 %% "fs2-core" % V.fs2
  lazy val quillJdbc = O.quill %% "quill-jdbc" % V.quill
  lazy val quillDoobie = O.quill %% "quill-doobie" % V.quill
  lazy val sqliteJdbc = O.xerial % "sqlite-jdbc" % V.sqliteJdbc
  lazy val doobieCore = O.tpolecat %% "doobie-core" % V.doobie
  lazy val doobieHikari = O.tpolecat %% "doobie-hikari" % V.doobie
  lazy val logstage = O.izumi %% "logstage-core" % V.izumi
  lazy val logstageSlf4j = O.izumi %% "logstage-adapter-slf4j" % V.izumi
  lazy val fly4s = O.geirolz %% "fly4s" % V.fly4s
  lazy val chimney = O.scalaland %% "chimney" % V.chimney
  lazy val munit = O.scalameta %% "munit" % V.munit
}
