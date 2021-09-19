package maze.app.protocol

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.{TestKit, TestProbe}
import akka.util.ByteString
import maze.app.model.TestData.{examplePayLoadWholeString, expectedSplitMessages}
import maze.app.protocol.Protocol.lineSplitter
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt

object ProtocolTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  private implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName)

  import system.dispatcher

  override def afterAll: Unit = TestKit.shutdownActorSystem(system)

  behavior of "lineSplitter"

  it should "separate into lines" in {
    val probe: TestProbe = TestProbe()
    import akka.pattern.pipe
    val oneLineSource: Source[ByteString, NotUsed] = Source.single(examplePayLoadWholeString).map(ByteString(_))
    oneLineSource.via(lineSplitter).runWith(Sink.seq).pipeTo(probe.ref)
    probe.expectMsg(3.seconds, expectedSplitMessages)
  }

}
