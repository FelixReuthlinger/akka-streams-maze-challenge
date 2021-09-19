package maze.app

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Tcp.IncomingConnection
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

  val usersConnected: Future[Done] = userClientConnections.runForeach((userClientConnection: IncomingConnection) => {
    println(s"User client connection from: ${userClientConnection.remoteAddress}")
    userClientConnection.handleWith(
      Flow[ByteString]
        .via(lineSplitterSimple)
        .via(userParser)
        .alsoTo(logSinkUserSignIn)
        .via(Flow.fromFunction(_ => ByteString("out"))))
  })

  val eventsProcessed: Future[Done] = eventSourceConnections.runForeach((eventConnection: IncomingConnection) => {
    println(s"Event source connection from: ${eventConnection.remoteAddress}")
  })

  eventsProcessed.onComplete(_ => {
    // disconnect users
    // TODO
    // end ActorSystem
    system.terminate()
  })

}
