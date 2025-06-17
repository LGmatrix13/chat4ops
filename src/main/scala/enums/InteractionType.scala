package enums

import models.IncomingInteraction
import upickle.default.{macroRW, ReadWriter as RW}

enum InteractionType(val value: Int):
  case Ping extends InteractionType(1)
  case Slash extends InteractionType(2)
  case AcceptDecline extends InteractionType(3)
  case Form extends InteractionType(5)
