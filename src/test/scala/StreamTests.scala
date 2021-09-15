import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration.DurationInt

class StreamTests extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  private implicit val system: ActorSystem = ActorSystem("StreamTests")
  override def afterAll: Unit = TestKit.shutdownActorSystem(system)

  it should "" in {
    import akka.pattern.pipe
    import system.dispatcher

    val sourceUnderTest: Source[Seq[Int], NotUsed] = Source(1 to 4).grouped(2)

    val probe: TestProbe = TestProbe()
    sourceUnderTest.runWith(Sink.seq).pipeTo(probe.ref)
    probe.expectMsg(3.seconds, Seq(Seq(1, 2), Seq(3, 4)))
  }
}
