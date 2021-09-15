
import ProtocolParser._
import TcpConnector._
import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.util.ByteString

import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths
import scala.util.Try

object TcpConnector {

  private final val HOST: String = "localhost"
  final val EVENT_SOURCE_PORT: Int = 9090
  final val USER_CLIENT_PORT: Int = 9090

  def establishIncomingConnections(host: String = HOST, port: Int)(implicit system: ActorSystem):
  Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind(interface = host, port = port)

}

object MazeChallengeApp extends App {
  implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName)
  val eventSourceConnections: Source[IncomingConnection, Future[ServerBinding]] =
    TcpConnector.establishIncomingConnections(port = EVENT_SOURCE_PORT)
  val userClientConnections: Source[IncomingConnection, Future[ServerBinding]] =
    TcpConnector.establishIncomingConnections(port = USER_CLIENT_PORT)

  eventSourceConnections.runForeach((eventConnection: IncomingConnection) => {
    println(s"Event source connection from: ${eventConnection.remoteAddress}")
  })

  userClientConnections.runForeach((userClientConnection: IncomingConnection) => {
    println(s"User client connection from: ${userClientConnection.remoteAddress}")
  })

  val userSignInFlow = Flow[ByteString]
    .via(Framing.delimiter(ByteString(PROTOCOL_MESSAGE_SEPARATOR), maximumFrameLength = 256, allowTruncation = true))
    .map(_.utf8String)
    .map {
      case clientIdString@UserClient(client) => (clientIdString, Option(client))
      case otherString: String => (otherString, None)
    }

  val deserializeMessageFlow = Flow[ByteString]
    .via(Framing.delimiter(ByteString(PROTOCOL_MESSAGE_SEPARATOR), maximumFrameLength = 256, allowTruncation = true))
    .map(_.utf8String)
    .map {
      case messageString@MazeMessage(message) => (messageString, Option(message))
      case otherString: String => (otherString, None)
    }


}
