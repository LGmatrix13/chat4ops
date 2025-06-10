package utilities

import models.{ActionRow, Button, Message}
import sttp.client4.quick.*
import upickle.default._

object Discord {
  private final val rootUrl = "https://discord.com/api/v10"
  private final val botToken = sys.env.get("DISCORD_BOT_TOKEN")

  private def headers(): Unit = {
    basicRequest.header("Authorization", botToken)
    basicRequest.header("Content-Type", "application/json")
  }

  def sendAcceptDeclineMessage(channelId: String): Unit = {
    this.headers()
    val message = Message(
      content = "Click the button below!",
      components = List(
        ActionRow(
          components = List(
            Button(
              style = 1,
              label = "Accept",
              custom_id = "1"
            ),
            Button(
              style = 2,
              label = "Decline",
              custom_id = "2"
            )
          )
        )
      )
    )
    val jsonString: String = write(message)
    basicRequest.body("")
    quickRequest.post(uri"$rootUrl/channels/$channelId/messages")
  }

  def sendAcceptDeclineInteraction(interactionId: String, interactionToken: String): Unit = {
    this.headers()
    basicRequest.body("Hello, world!")
    quickRequest.post(uri"$rootUrl/interactions/$interactionId/$interactionToken/callback")
  }
}
