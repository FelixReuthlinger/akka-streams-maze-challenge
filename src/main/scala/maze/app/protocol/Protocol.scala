package maze.app.protocol

import akka.NotUsed
import akka.stream.ClosedShape
import akka.stream.javadsl.RunnableGraph
import akka.stream.scaladsl.{Flow, Framing, GraphDSL, Sink}
import akka.util.ByteString
import maze.app.MazeChallengeApp.system
import maze.app.model.Common.PROTOCOL_MESSAGE_SEPARATOR
import maze.app.model.{MazeMessage, UserClient}

object Protocol {

  final val lineSplitter: Flow[ByteString, ByteString, _] =
    Framing.delimiter(ByteString(PROTOCOL_MESSAGE_SEPARATOR), maximumFrameLength = 256, allowTruncation = true)
  final val lineSplitterSimple: Flow[ByteString, ByteString, _] =
    Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true)

  final val logSink: Sink[String, _] = Sink.foreach(system.log.info(_))
  final val logSinkByteString: Sink[ByteString, _] = Sink.foreach(bytes => system.log.info(bytes.utf8String))
  final val printSinkByteString: Sink[ByteString, _] = Sink.foreach(bytes => println(bytes.utf8String))
  final val logSinkMessage: Sink[MazeMessage, _] =
    Sink.foreach(message => system.log.info(message.toString))
  final val logSinkUserSignIn: Sink[UserClient, _] =
    Sink.foreach(client => system.log.info(s"client '$client' signed in"))

  private final val parserUser: ByteString => UserClient = _.utf8String match {
    case UserClient(client) => client
  }
  final val userParser: Flow[ByteString, UserClient, _] = Flow.fromFunction(parserUser)

  private final val parseMessage: ByteString => MazeMessage = _.utf8String match {
    case MazeMessage(message) => message
  }
  final val messageParser: Flow[ByteString, MazeMessage, _] = Flow.fromFunction(parseMessage)

  // final val messageSink: Sink[MazeMessage, Publisher[MazeMessage]] = Sink.asPublisher[MazeMessage](fanout = false)
  // final val messageMergeSource = Source.fromPublisher()

  val protocol = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>


    ClosedShape
  })
}
