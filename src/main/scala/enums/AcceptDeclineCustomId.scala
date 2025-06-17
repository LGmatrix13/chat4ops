package enums

import models.IncomingInteraction

enum AcceptDeclineCustomId(val value: String):
  case Accept extends AcceptDeclineCustomId("ACCEPT")
  case Decline extends AcceptDeclineCustomId("DECLINE")
