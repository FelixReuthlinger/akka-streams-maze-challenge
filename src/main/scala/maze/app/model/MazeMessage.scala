package maze.app.model

import maze.app.model.Common.{PROTOCOL_MESSAGE_ELEMENT_SEPARATOR, PROTOCOL_MESSAGE_ESCAPE, messageElements}

import scala.util.Try

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
