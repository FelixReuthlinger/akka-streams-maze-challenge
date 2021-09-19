package maze.app.model

import akka.util.ByteString
import maze.app.model.Common.PROTOCOL_MESSAGE_SEPARATOR

object TestData {

  final val exampleUserClientMessage1: String = "2932"
  final val exampleUserClientMessage2: String = "asd"
  final val exampleUserClientMessage3: String = ""
  final val exampleUserClientMessages: Array[String] =
    Array(exampleUserClientMessage1, exampleUserClientMessage2, exampleUserClientMessage3)
  final val userClient1 = UserClient(clientId = 2932)

  final val expectedUserClientsParsed: Seq[(String, Option[UserClient])] = Seq(
    (exampleUserClientMessage1, Option(userClient1)),
    (exampleUserClientMessage2, None),
    (exampleUserClientMessage3, None)
  )

  final val examplePayload1 = "666\\|F\\|60\\|50"
  final val examplePayload2 = "1\\|U\\|12\\|9"
  final val examplePayload3 = "542532\\|B"
  final val examplePayload4 = "43\\|P\\|32\\|56"
  final val examplePayload5 = "634\\|S\\|32"
  final val examplePayload6BadType = "634\\|xxx\\|32\\|33"
  final val examplePayload: Array[String] =
    Array(examplePayload1, examplePayload2, examplePayload3, examplePayload4, examplePayload5)
  final val examplePayloadWithBadData: Array[String] = examplePayload ++ Array(examplePayload6BadType)


  final val examplePayLoadWholeString: String = examplePayload.mkString(PROTOCOL_MESSAGE_SEPARATOR)
  final val expectedSplitMessages: Seq[ByteString] = Seq(ByteString(examplePayload1), ByteString(examplePayload2),
    ByteString(examplePayload3), ByteString(examplePayload4), ByteString(examplePayload5))

  final val exampleMessage1 = MazeMessage(sequenceId = 666, messageType = Follow, fromUser = Option(60), toUser = Option(50))
  final val exampleMessage2 = MazeMessage(sequenceId = 1, messageType = Unfollow, fromUser = Option(12), toUser = Option(9))
  final val exampleMessage3 = MazeMessage(sequenceId = 542532, messageType = Broadcast, fromUser = None, toUser = None)
  final val exampleMessage4 = MazeMessage(sequenceId = 43, messageType = PrivateMessage, fromUser = Option(32), toUser = Option(56))
  final val exampleMessage5 = MazeMessage(sequenceId = 634, messageType = StatusUpdate, fromUser = Option(32), toUser = None)
  final val exampleMessages: Array[MazeMessage] =
    Array(exampleMessage1, exampleMessage2, exampleMessage3, exampleMessage4, exampleMessage5)

  final val expectedMessagesParsed: Seq[(String, Option[MazeMessage])] = Seq(
    (examplePayload1, Option(exampleMessage1)),
    (examplePayload2, Option(exampleMessage2)),
    (examplePayload3, Option(exampleMessage3)),
    (examplePayload4, Option(exampleMessage4)),
    (examplePayload5, Option(exampleMessage5)),
    (examplePayload6BadType, None)
  )
}
