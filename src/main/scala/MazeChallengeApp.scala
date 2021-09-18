
import ProtocolParser.{UserClient, userParser}
import TcpConnector._
import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl._

import scala.concurrent._

object MazeChallengeApp extends App {

  implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName)
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  val userConnectionRegister = Map.empty[UserClient, IncomingConnection]
  TcpConnector
    .establishIncomingConnections(port = USER_CLIENT_PORT)
    .runForeach(f = (userClientConnection: IncomingConnection) => {
      println(s"User client connection from: ${userClientConnection.remoteAddress}")
    })

  val eventSourceConnections: Source[IncomingConnection, Future[ServerBinding]] =
    TcpConnector.establishIncomingConnections(port = EVENT_SOURCE_PORT)
  eventSourceConnections.runForeach((eventConnection: IncomingConnection) => {
    println(s"Event source connection from: ${eventConnection.remoteAddress}")
  })
}
