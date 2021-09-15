import ProtocolParser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ProtocolParserTest extends AnyFlatSpec with Matchers {

  private final val exampleUserClientMessage1: String = "2932"
  private final val exampleUserClientMessage2: String = "asd"
  private final val exampleUserClientMessage3: String = ""
  private final val exampleUserClientMessages: Array[String] =
    Array(exampleUserClientMessage1, exampleUserClientMessage2, exampleUserClientMessage3)
  private final val userClient1 = UserClient(clientId = 2932)

  private final val examplePayload1 = "666\\|F\\|60\\|50"
  private final val examplePayload2 = "1\\|U\\|12\\|9"
  private final val examplePayload3 = "542532\\|B"
  private final val examplePayload4 = "43\\|P\\|32\\|56"
  private final val examplePayload5 = "634\\|S\\|32"
  private final val examplePayload6BadType = "634\\|xxx\\|32\\|33"
  private final val examplePayload: Array[String] =
    Array(examplePayload1, examplePayload2, examplePayload3, examplePayload4, examplePayload5, examplePayload6BadType)
  private final val examplePayLoadWholeString: String = examplePayload.mkString(PROTOCOL_MESSAGE_SEPARATOR)

  private final val exampleMessage1 = MazeMessage(sequenceId = 666, messageType = Follow, fromUser = Option(60), toUser = Option(50))
  private final val exampleMessage2 = MazeMessage(sequenceId = 1, messageType = Unfollow, fromUser = Option(12), toUser = Option(9))
  private final val exampleMessage3 = MazeMessage(sequenceId = 542532, messageType = Broadcast, fromUser = None, toUser = None)
  private final val exampleMessage4 = MazeMessage(sequenceId = 43, messageType = PrivateMessage, fromUser = Option(32), toUser = Option(56))
  private final val exampleMessage5 = MazeMessage(sequenceId = 634, messageType = StatusUpdate, fromUser = Option(32), toUser = None)
  private final val exampleMessages: Array[MazeMessage] =
    Array(exampleMessage1, exampleMessage2, exampleMessage3, exampleMessage4, exampleMessage5)


  behavior of "UserClient"

  it should "be able to use unapply in pattern matching" in {
    val result: UserClient = exampleUserClientMessage1 match {
      case UserClient(client) => client
    }
    result shouldBe userClient1
  }

  it should "only parse valid messages to clients" in {
    exampleUserClientMessages.flatMap{
      case UserClient(client) => Option(client)
      case _ => None
    } should contain theSameElementsAs Array(userClient1)
  }

  behavior of "MazeMessage"

  it should "be able to use unapply in pattern matching" in {
    val result: MazeMessage = examplePayload1 match {
      case MazeMessage(message) => message
    }
    result shouldBe exampleMessage1
  }

  it should "parse all types of proper messages to proper results, discarding bad messages" in {
    examplePayload
      .flatMap {
        case MazeMessage(message) => Option(message)
        case _ => None
      } should contain theSameElementsAs exampleMessages
  }
}
