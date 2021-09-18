import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString

import scala.util.Try

/**
 * Parsing utility for de-/serializing message Strings.
 */
object ProtocolParser {

  final val PROTOCOL_MESSAGE_SEPARATOR = "\r\n"
  final val PROTOCOL_MESSAGE_ESCAPE = "\\"
  final val PROTOCOL_MESSAGE_ELEMENT_SEPARATOR = '|'

  final def messageElements(message: String): Array[String] =
    message
      .replace(PROTOCOL_MESSAGE_ESCAPE, "")
      .split(PROTOCOL_MESSAGE_ELEMENT_SEPARATOR)

  final case class UserClient(clientId: Int) {
    override def toString: String = clientId.toString
  }

  object UserClient {
    def unapply(protocolMessage: String): Option[UserClient] = {
      messageElements(protocolMessage).toList match {
        case head :: Nil => Try(UserClient(clientId = head.toInt)).toOption
        case _ => None
      }
    }
  }

  final val userParser: Flow[ByteString, (String, Option[UserClient]), NotUsed] = Flow[ByteString].map(_.utf8String)
    .map {
      case clientIdString@UserClient(client) => (clientIdString, Option(client))
      case otherString: String => (otherString, None)
    }

  final case class MazeMessage(sequenceId: Long, messageType: MessageType, fromUser: Option[Int], toUser: Option[Int]) {
    override def toString: String =
      (Seq(sequenceId.toString, messageType.toString) ++
        Seq(fromUser, toUser).flatten.map(_.toString)).
        mkString(s"$PROTOCOL_MESSAGE_ESCAPE$PROTOCOL_MESSAGE_ELEMENT_SEPARATOR")
  }

  object MazeMessage {
    def unapply(protocolMessage: String): Option[MazeMessage] = messageElements(protocolMessage).toList match {
      case sequenceId :: Broadcast() :: Nil =>
        Try(MazeMessage(sequenceId.toInt, Broadcast, None, None)).toOption
      case sequenceId :: StatusUpdate() :: fromUser :: Nil =>
        Try(MazeMessage(sequenceId.toInt, StatusUpdate, Option(fromUser.toInt), None)).toOption
      case sequenceId :: Follow() :: fromUser :: toUser :: Nil =>
        Try(MazeMessage(sequenceId.toInt, Follow, Option(fromUser.toInt), Option(toUser.toInt))).toOption
      case sequenceId :: Unfollow() :: fromUser :: toUser :: Nil =>
        Try(MazeMessage(sequenceId.toInt, Unfollow, Option(fromUser.toInt), Option(toUser.toInt))).toOption
      case sequenceId :: PrivateMessage() :: fromUser :: toUser :: Nil =>
        Try(MazeMessage(sequenceId.toInt, PrivateMessage, Option(fromUser.toInt), Option(toUser.toInt))).toOption
      case _ => None
    }
  }

  final val eventMessageParser: Flow[ByteString, (String, Option[MazeMessage]), NotUsed] =
    Flow[ByteString].map(_.utf8String).map {
      case messageString@MazeMessage(message) => (messageString, Option(message))
      case otherString: String => (otherString, None)
    }

}
