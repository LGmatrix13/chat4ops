package utilities

import sttp.client4.quick.*

object Discord {
  private final val rootUrl = "https://discord.com/api/v10"
  private final val botToken = ""

  private def headers(): Unit = {
    basicRequest.header("Authorization", botToken)
    basicRequest.header("Content-Type", "application/json")
  }

  def sendAcceptDeclineMessage(channelId: String): Unit = {
    this.headers()
    basicRequest.body("Hello, world!")
    quickRequest.post(uri"$rootUrl/channels/$channelId/messages")
  }

  def sendAcceptDeclineInteraction(interactionId: String, interactionToken: String): Unit = {
    this.headers()
    basicRequest.body("Hello, world!")
    quickRequest.post(uri"$rootUrl/interactions/$interactionId/$interactionToken/callback")
  }
}
