import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MessageTypeTest extends AnyFlatSpec with Matchers {

  private final val testTypeStrings: Seq[String] = Seq("F", "U", "P", "B", "S")
  private final val testTypeEnums: Seq[MessageType] = Seq(Follow, Unfollow, Broadcast, PrivateMessage, StatusUpdate)

  behavior of "fromString"

  it should "transform to enum" in {
    testTypeStrings.
      map(MessageType.fromString) should contain theSameElementsAs testTypeEnums

  }

  it should "fail on bad input" in {
    val exception: IllegalArgumentException = intercept[IllegalArgumentException]{
      MessageType.fromString(input =  "abc")
    }
    exception.getMessage shouldBe "message type 'abc' not supported"
  }

  behavior of "unapply"

  it should "do match patterns" in {
    val results = testTypeStrings.map{
      case Follow() => 1
      case Unfollow() => 2
      case Broadcast() => 3
      case PrivateMessage() => 4
      case StatusUpdate() => 5
      case _ => fail("wait what?")
    }
    results.length shouldBe 5
    results.sum shouldBe 15
  }

  behavior of "toString"

  it should "also convert back to the string chars" in {
    testTypeEnums.map(_.toString) should contain theSameElementsAs testTypeStrings
  }
}
