import enums.{InputType, InteractionType}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import models._
import sttp.shared.Identity
import sttp.tapir.server.netty.sync.NettySyncServer
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import utilities.{Chat4Ops, DiscordBot, EnvLoader}
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.netty.NettyConfig
import io.circe.parser.*

import scala.concurrent.duration.*

sealed trait ErrorInfo

case class NotFound() extends ErrorInfo
object NotFound {
  implicit val decoder: Decoder[NotFound] = deriveDecoder
  implicit val encoder: Encoder[NotFound] = deriveEncoder
}
case class Unauthorized() extends ErrorInfo
object Unauthorized {
  implicit val decoder: Decoder[Unauthorized] = deriveDecoder
  implicit val encoder: Encoder[Unauthorized] = deriveEncoder
}
case class BadRequest() extends ErrorInfo
object BadRequest {
  implicit val decoder: Decoder[BadRequest] = deriveDecoder
  implicit val encoder: Encoder[BadRequest] = deriveEncoder
}

object Chat4OpsServer {

  private val baseEndpoint = endpoint.errorOut(
    oneOf[ErrorInfo](
      oneOfVariant(StatusCode.NotFound, jsonBody[NotFound].description("not found")),
      oneOfVariant(StatusCode.Unauthorized, jsonBody[Unauthorized].description("unauthorized")),
      oneOfVariant(StatusCode.BadRequest, jsonBody[BadRequest].description("bad request")),
    )
  )

  private val acceptDeclineEndpoint = baseEndpoint
    .get
    .in("api" / "send" / "accept-decline")
    .out(stringBody)
    .handleSuccess(_ => {
      val success = Chat4Ops.executeAction(
        action = AcceptDeclineAction(
          channelId = "1381992880834351184",
          message = "Please make a decision"
        )
      )
      if success then "Success" else "Failure"
    })

   private val formEndpoint = baseEndpoint
    .get
    .in("api" / "send" / "form")
    .out(stringBody)
    .handleSuccess(_ => {
      val success = Chat4Ops.executeAction(
        action = FormAction(
          channelId = "1381992880834351184",
          inputs = Seq(
            Input(
              label = "Name",
              required = true,
              `type` = InputType.Text
            )
          )
        )
      )
      if success then "Success" else "Failure"
    })

  private val interactionsEndpoint = baseEndpoint
    .post
    .in("api" / "interactions")
    .in(header[String]("X-Signature-Ed25519"))
    .in(header[String]("X-Signature-Timestamp"))
    .in(stringBody)
    .out(jsonBody[InteractionResponse])
    .handle { case (signature, timestamp, body) =>
      val isValid = DiscordBot.verifySignature(
        signature,
        timestamp,
        body
      )
      if (!isValid) {
        Left(Unauthorized())
      } else {
        println(body)
        val decoded = decode[InteractionRequest](body)
        decoded match {
          case Right(interactionRequest) =>
            val interactionResponse = Chat4Ops.executeInteraction(
              interactionRequest = interactionRequest,
              interactions = Interactions(
                acceptDeclineInteraction = Some(AcceptDeclineInteraction(
                  decliningMessage = "You decline!",
                  acceptingMessage = "You Accepted!",
                  ephemeral = false
                )),
                slashInteraction = Some(SlashInteraction(
                  message = "Responding to your slash command!",
                  ephemeral = true
                ))
              )
            )
            if interactionResponse.isDefined then Right(interactionResponse.get) else Left(BadRequest())
          case Left(error) =>
            println(error)
            Left(BadRequest())
        }
      }
    }
  // Add shutdown hook to clean up server
  def start(): Unit = {
    val config = NettyConfig.default.withGracefulShutdownTimeout(2.seconds)
    val endpoints = List(this.acceptDeclineEndpoint, this.interactionsEndpoint, this.formEndpoint)
//    Chat4Ops.executeRegistration(
//      registration = SlashRegistration(
//        name = "ping",
//        description = "The ping slash command"
//      )
//    )
    val swaggerEndpoints = SwaggerInterpreter()
      .fromServerEndpoints[Identity](endpoints, "Chat4OpsServer", "1.0")
    NettySyncServer(config)
      .port(8080)
      .addEndpoints(endpoints)
      .addEndpoints(swaggerEndpoints)
      .startAndWait()
  }
}

@main def main(): Unit = {
  EnvLoader.loadEnv("./src/.env")
  Chat4OpsServer.start()
}

