sealed trait MessageType {
  final def unapply(typeString: String): Boolean = typeString == toString
}

object MessageType {

  final val C2C_TYPES: Seq[MessageType] = Seq(Follow, Unfollow, PrivateMessage)

  def fromString(input: String): Option[MessageType] = input match {
    case Follow() => Option(Follow)
    case Unfollow() => Option(Unfollow)
    case Broadcast() => Option(Broadcast)
    case PrivateMessage() => Option(PrivateMessage)
    case StatusUpdate() => Option(StatusUpdate)
    case _ => None
  }
}

case object Follow extends MessageType {
  override final val toString: String = "F"
}

case object Unfollow extends MessageType {
  override final val toString: String = "U"
}

case object Broadcast extends MessageType {
  override final val toString: String = "B"
}

case object PrivateMessage extends MessageType {
  override final val toString: String = "P"
}

case object StatusUpdate extends MessageType {
  override final val toString: String = "S"
}
