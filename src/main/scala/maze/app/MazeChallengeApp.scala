package maze.app

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.IncomingConnection
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import maze.app.model.UserClient
import maze.app.protocol.Protocol._

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

  val eventFlow: Flow[ByteString, ByteString, _] = Flow[ByteString]
    .via(lineSplitterSimple)
    .via(messageParser)
    .alsoTo(logSinkMessagePassed)
    .alsoTo(Sink.asPublisher(false))
    .via(Flow.fromFunction(_ => ByteString("")))

  val userFlow: Flow[ByteString, ByteString, _] = Flow[ByteString]
    .via(lineSplitterSimple)
    .via(userParser)
    .alsoTo(logSinkUserSignIn)
    .merge(Source.fromPublisher(eventFlow))

  val usersConnected: Future[Done] = userClientConnections.runForeach((userClientConnection: IncomingConnection) => {
    println(s"User client connection from: ${userClientConnection.remoteAddress}")
    userClientConnection.handleWith(userFlow)
  })

  val eventsProcessed: Future[Done] = eventSourceConnections.runForeach((eventConnection: IncomingConnection) => {
    println(s"Event source connection from: ${eventConnection.remoteAddress}")
    eventConnection.handleWith(eventFlow)
  })

  eventsProcessed.onComplete(_ => {
    // disconnect users
    // TODO
    // end ActorSystem
    system.terminate()
  })

}
