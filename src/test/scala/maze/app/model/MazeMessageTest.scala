package maze.app.model

import maze.app.model.TestData._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MazeMessageTest extends AnyFlatSpec with Matchers {

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
