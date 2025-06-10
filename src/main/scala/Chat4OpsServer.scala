import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.server.netty.sync.NettySyncServer
import sttp.tapir.swagger.bundle.SwaggerInterpreter

@main def main(): Unit =
  val sendEndpoint = endpoint
    .get
    .in("api" / "send")
    .out(stringBody)
    .handleSuccess(_ => "Success")

  val respondEndpoint = endpoint
    .post
    .in("api" / "respond")
    .out(stringBody)
    .handleSuccess(_ => "Success")

  val endpoints = List(sendEndpoint)
  val swaggerEndpoints = SwaggerInterpreter()
    .fromServerEndpoints[Identity](endpoints, "Chat4Ops", "1.0")

  NettySyncServer()
    .port(8080)
    .addEndpoints(endpoints)
    .addEndpoints(swaggerEndpoints)
    .startAndWait()