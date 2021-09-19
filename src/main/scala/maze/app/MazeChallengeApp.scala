package maze.app


import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Tcp.IncomingConnection
import maze.app.model.UserClient

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
