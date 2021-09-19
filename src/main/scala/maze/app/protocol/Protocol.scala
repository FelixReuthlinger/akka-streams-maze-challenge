package maze.app.protocol

import akka.stream.scaladsl.{Flow, Framing, Sink}
import akka.util.ByteString
import maze.app.MazeChallengeApp.system
import maze.app.model.Common.PROTOCOL_MESSAGE_SEPARATOR
import maze.app.model.{MazeMessage, UserClient}

object Protocol {

  final val lineSplitter: Flow[ByteString, ByteString, _] =
    Framing.delimiter(ByteString(PROTOCOL_MESSAGE_SEPARATOR), maximumFrameLength = 256, allowTruncation = true)

  final val logSink: Sink[String, _] = Sink.foreach(system.log.info(_))
  final val logSinkMessage: Sink[MazeMessage, _] =
    Sink.foreach(message => system.log.info(message.toString))
  final val logSinkUserSignIn: Sink[UserClient, _] =
    Sink.foreach(client => system.log.info(s"client '$client' signed in"))
}
