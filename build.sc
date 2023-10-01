// // build.sc
import mill._, scalalib._
import os.Path

object Reg_Rename extends ScalaModule {
  def scalaVersion = "2.12.12"
  override def millSourcePath = os.pwd
  def scalaOptions = Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    "-P:chiselplugin:genBundleElements",
    "-Xsource:2.11"
  )

  override def ivyDeps = Agg(
    ivy"edu.berkeley.cs::chisel3:3.5.0"
  )

  override def scalacPluginIvyDeps = Agg(
    ivy"edu.berkeley.cs:::chisel3-plugin:3.5.0",
  )

}