import enums.{InputType, InteractionType}
import models.*
import sttp.shared.Identity
import sttp.tapir.server.netty.sync.NettySyncServer
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import utilities.{Chat4Ops, DiscordBot, EnvLoader}
import sttp.tapir.*
import sttp.tapir.json.upickle.*
import sttp.tapir.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.server.netty.NettyConfig
import upickle.default.{macroRW, ReadWriter as RW}
import upickle.default.*
import scala.concurrent.duration.*

sealed trait ErrorInfo
case class NotFound() extends ErrorInfo
object NotFound {
  implicit val rw: RW[NotFound] = macroRW
}

case class Unauthorized() extends ErrorInfo
object Unauthorized {
  implicit val rw: RW[Unauthorized] = macroRW
}

case class BadRequest() extends ErrorInfo
object BadRequest {
  implicit val rw: RW[BadRequest] = macroRW
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
        try {
          println(body)
          val incomingInteraction = read[IncomingInteraction](body)
          val interactionResponse = Chat4Ops.executeInteraction(
            incomingInteraction = incomingInteraction,
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
        } catch {
          case e: Throwable => {
            println(body)
            println(e)
            Left(BadRequest())
          }
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

