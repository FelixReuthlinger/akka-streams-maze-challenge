package maze.app.model

object Common {

  final val PROTOCOL_MESSAGE_SEPARATOR = "\r\n"
  final val PROTOCOL_MESSAGE_ESCAPE = "\\"
  final val PROTOCOL_MESSAGE_ELEMENT_SEPARATOR = '|'

  final def messageElements(message: String): Array[String] =
    message
      .replace(PROTOCOL_MESSAGE_ESCAPE, "")
      .split(PROTOCOL_MESSAGE_ELEMENT_SEPARATOR)

}
