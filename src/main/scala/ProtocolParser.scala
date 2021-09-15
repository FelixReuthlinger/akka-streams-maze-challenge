import MessageType.C2C_TYPES

import scala.util.Try

/**
 * Parsing utility for deserializing message Strings.
 */
object ProtocolParser {

  final val PROTOCOL_MESSAGE_SEPARATOR = "\r\n"
  final val PROTOCOL_MESSAGE_ELEMENT_SEPARATOR = '|'

  final def messageElements(message: String): Array[String] =
    message.replace("\\", "")
      .split(PROTOCOL_MESSAGE_ELEMENT_SEPARATOR)

  case class UserClient(clientId: Int){
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

  case class MazeMessage(sequenceId: Long, messageType: MessageType, fromUser: Option[Int], toUser: Option[Int]) {
    override def toString: String =
      (Seq(sequenceId.toString, messageType.toString) ++
        Seq(fromUser, toUser).flatten.map(_.toString)).
        mkString(PROTOCOL_MESSAGE_SEPARATOR)

  }

  object MazeMessage {
    def unapply(protocolMessage: String): Option[MazeMessage] = messageElements(protocolMessage).toList match {
      case sequenceId :: Broadcast() :: Nil =>
        Try(MazeMessage(sequenceId.toInt, Broadcast, None, None)).toOption
      case sequenceId :: StatusUpdate() :: fromUser :: Nil =>
        Try(MazeMessage(sequenceId.toInt, StatusUpdate, Option(fromUser.toInt), None)).toOption
      case sequenceId :: messageType :: fromUser :: toUser :: Nil if Seq(Follow, Unfollow, PrivateMessage).contains(messageType)=>
        Try(
          MazeMessage(sequenceId.toInt,
            (Follow|Unfollow|PrivateMessage),
            Option(fromUser.toInt),
            Option(toUser.toInt))
        ).toOption
      case _ => None
    }
  }

}
