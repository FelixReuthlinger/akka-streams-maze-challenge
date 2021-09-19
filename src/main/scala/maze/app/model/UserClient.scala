package maze.app.model

import maze.app.model.Common.messageElements

import scala.util.Try

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