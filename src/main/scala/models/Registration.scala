package models

import upickle.default.{macroRW, ReadWriter as RW}

trait Registration:
  def `type`: Int

case class SlashRegistration(
   name: String,
   description: String
) extends Registration:
  override val `type`: Int = 4
object SlashRegistration {
  implicit val rw: RW[SlashRegistration] = macroRW
}
