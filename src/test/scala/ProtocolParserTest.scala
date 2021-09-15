import ProtocolParser._
import TestData._
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.{TestKit, TestProbe}
import akka.util.ByteString
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import akka.pattern.pipe

import scala.concurrent.duration.DurationInt

class ProtocolParserTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  private implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName)
  import system.dispatcher
  override def afterAll: Unit = TestKit.shutdownActorSystem(system)

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

  behavior of "lineSplitter"

  it should "separate into lines" in {
    val probe: TestProbe = TestProbe()
    val oneLineSource: Source[ByteString, NotUsed] = Source.single(examplePayLoadWholeString).map(ByteString(_))
    oneLineSource.via(lineSplitter).runWith(Sink.seq).pipeTo(probe.ref)
    probe.expectMsg(3.seconds, expectedSplitMessages)
  }

  behavior of "userParser"

  it should "parse a stream of user sign-ins" in {
    val probe: TestProbe = TestProbe()
    val userSignIns: Source[ByteString, NotUsed] = Source(exampleUserClientMessages).map(ByteString(_))
    userSignIns.via(userParser).runWith(Sink.seq).pipeTo(probe.ref)
    probe.expectMsg(3.seconds, expectedUserClientsParsed)
  }

  behavior of "eventMessageParser"

  it should "parse an event message stream" in {
    val probe: TestProbe = TestProbe()
    val eventMessages: Source[ByteString, NotUsed] = Source(examplePayloadWithBadData).map(ByteString(_))
    eventMessages.via(eventMessageParser).runWith(Sink.seq).pipeTo(probe.ref)
    probe.expectMsg(3.seconds, expectedMessagesParsed)
  }
}
