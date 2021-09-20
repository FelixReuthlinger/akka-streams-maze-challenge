package maze.app

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Tcp.IncomingConnection
import akka.util.ByteString
import maze.app.model.{Broadcast, Follow, MazeMessage, PrivateMessage, StatusUpdate, UserClient}
import maze.app.protocol.Protocol._
import maze.app.protocol.Sinks.{logSinkMessage, logSinkUserSignIn}

import scala.concurrent._

trait SimplerClassName {
  def getSimplerClassName: String = getClass.getSimpleName.replaceAll("[^\\w]", "")
}

trait SimpleActorSystem extends SimplerClassName {
  implicit val system: ActorSystem = ActorSystem(getSimplerClassName)
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
}

object MazeChallengeApp extends App with SimpleActorSystem {

  val userConnectionRegister: Map[UserClient, IncomingConnection] = Map.empty

  val (eventSourceConnections, userClientConnections) = TcpConnector.setupConnections()

  val follower: Map[Int, List[Int]] = Map(123 -> List(234, 456))

  val userSignInReceiveMessagesFlow: Flow[ByteString, ByteString, _] =
    Flow[ByteString]
      .via(lineSplitterSimple)
      .via(userParser)
      .alsoTo(logSinkUserSignIn)
      //.merge() --> somehow merge the event stream in here via Sink "publisherSink" -> Publisher -> Source
      .map(client => (client, MazeMessage(sequenceId = 123, messageType = Follow, fromUser = Option(234), toUser = Option(345))))
      .filter { case (client: model.UserClient, message: model.MazeMessage) =>
        (message.messageType, message.toUser, message.fromUser) match {
          case (Follow, Some(to), Some(from)) =>
            // something to add follow to list -> follower ++ Map(to -> from)
            client.clientId == to
          case (Broadcast, _, _) => true
          case (PrivateMessage, Some(to), _) => client.clientId == to
          case (StatusUpdate, _, Some(from)) => follower(from).contains(client.clientId)
          case _ => false
        }
      }.map { case (_, message: model.MazeMessage) => ByteString(message.toString) }

  val eventsInputOutputFlow: Flow[ByteString, ByteString, _] =
    Flow[ByteString]
      .via(lineSplitterSimple)
      .via(messageParser)
      .alsoTo(logSinkMessage)
      //.alsoTo(publisherSink)
      .map(_ => ByteString.empty)
      .filter(_ => false)

  val usersConnected: Future[Done] = userClientConnections.runForeach((userClientConnection: IncomingConnection) => {
    println(s"User client connection from: ${userClientConnection.remoteAddress}")
    userClientConnection.handleWith(userSignInReceiveMessagesFlow)
  })

  val eventsProcessed: Future[Done] = eventSourceConnections.runForeach((eventConnection: IncomingConnection) => {
    println(s"Event source connection from: ${eventConnection.remoteAddress}")
    eventConnection.handleWith(eventsInputOutputFlow)
  })

  eventsProcessed.onComplete(_ => {
    system.terminate()
  })

}
