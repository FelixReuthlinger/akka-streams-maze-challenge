import ProtocolParser._
import TestData._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ProtocolParserTest extends AnyFlatSpec with Matchers {

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
    examplePayloadWithBadData
      .flatMap {
        case MazeMessage(message) => Option(message)
        case _ => None
      } should contain theSameElementsAs exampleMessages
  }

  it should "de-ser properly (not changing input)" in {
    examplePayload.flatMap {
      case MazeMessage(message) => Option(message)
      case _ => None
    }.map(_.toString) should contain theSameElementsAs examplePayload
  }
}
