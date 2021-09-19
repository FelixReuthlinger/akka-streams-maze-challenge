package maze.app.model

import maze.app.model.TestData.{exampleUserClientMessage1, exampleUserClientMessages, userClient1}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UserClientTest extends AnyFlatSpec with Matchers {

  it should "be able to use unapply in pattern matching" in {
    val result: UserClient = exampleUserClientMessage1 match {
      case UserClient(client) => client
    }
    result shouldBe userClient1
  }

  it should "only parse valid messages to clients" in {
    exampleUserClientMessages.flatMap {
      case UserClient(client) => Option(client)
      case _ => None
    } should contain theSameElementsAs Array(userClient1)
  }
}
