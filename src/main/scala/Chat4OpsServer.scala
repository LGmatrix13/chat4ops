import enums.InputType
import models.*
import sttp.shared.Identity
import sttp.tapir.server.netty.sync.NettySyncServer
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import utilities.{Chat4Ops, DiscordBot, EnvLoader}
import sttp.tapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto.*
import sttp.model.StatusCode
import io.circe.generic.auto.*
import upickle.default.read

sealed trait ErrorInfo
case class NotFound() extends ErrorInfo
case class Unauthorized() extends ErrorInfo
case class BadRequest() extends ErrorInfo
case object NoContent extends ErrorInfo

val baseEndpoint: Endpoint[Unit, Unit, ErrorInfo, Unit, Any] = endpoint.errorOut(
  oneOf[ErrorInfo](
    oneOfVariant(StatusCode.NotFound, jsonBody[NotFound].description("not found")),
    oneOfVariant(StatusCode.Unauthorized, jsonBody[Unauthorized].description("unauthorized")),
    oneOfVariant(StatusCode.BadRequest, jsonBody[BadRequest].description("bad request")),
    oneOfVariant(StatusCode.NoContent, emptyOutputAs(NoContent)),
  )
)

@main def main(): Unit =
  EnvLoader.loadEnv("./src/.env")

  val acceptDeclineEndpoint = baseEndpoint
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

  val formEndpoint = baseEndpoint
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


  val respondEndpoint = baseEndpoint
    .post
    .in("api" / "interactions")
    .in(header[String]("X-Signature-Ed25519"))
    .in(header[String]("X-Signature-Timestamp"))
    .in(stringBody)
    .out(stringBody)
    .handle { case (signature, timestamp, body) =>
      val isValid = DiscordBot.verifySignature(
        signature,
        timestamp,
        body
      )
      if (!isValid) {
        Left(Unauthorized())
      } else {
        val incomingInteraction = read[IncomingInteraction](body)
        val success = Chat4Ops.executeInteraction(
          incomingInteraction = incomingInteraction,
          interactions = Interactions(
            acceptDeclineInteraction = Some(AcceptDeclineInteraction(
              decliningMessage = "You decline!",
              acceptingMessage = "You Accepted!"
            ))
          )
        )
        if success then Right("Success") else Left(BadRequest())
      }
    }

  val endpoints = List(acceptDeclineEndpoint, formEndpoint, respondEndpoint)
  val swaggerEndpoints = SwaggerInterpreter()
    .fromServerEndpoints[Identity](endpoints, "Chat4Ops", "1.0")

  NettySyncServer()
    .port(8080)
    .addEndpoints(endpoints)
    .addEndpoints(swaggerEndpoints)
    .startAndWait()