package maze.app

import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl.{Source, Tcp}

import scala.concurrent.Future

object TcpConnector {

  private final val HOST: String = "localhost"
  final val EVENT_SOURCE_PORT: Int = 9090
  final val USER_CLIENT_PORT: Int = 9099

  def establishIncomingConnections(host: String = HOST, port: Int)(implicit system: ActorSystem):
  Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind(interface = host, port = port)

  def setupConnections()(implicit system: ActorSystem):
  (Source[IncomingConnection, Future[ServerBinding]], Source[IncomingConnection, Future[ServerBinding]]) =
    (establishIncomingConnections(port = EVENT_SOURCE_PORT), establishIncomingConnections(port = EVENT_SOURCE_PORT))
}
