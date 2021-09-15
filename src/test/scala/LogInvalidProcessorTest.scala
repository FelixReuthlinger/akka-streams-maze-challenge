import akka.NotUsed
import akka.actor.ActorSystem
import akka.pattern.pipe
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

case class Test(value: Int)

class LogInvalidProcessorTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  private implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName)

  import system.dispatcher

  override def afterAll: Unit = TestKit.shutdownActorSystem(system)

  private final val testMaybeElements: Seq[Option[Test]] = Seq(Option(Test(1)), Option(Test(2)), None, Option(Test(3)))
  private final val testElements: Seq[Test] = testMaybeElements.flatten
  private final val testStingElements: Seq[(String, Option[Test])] = Seq("1", "2", "-1", "3").zip(testMaybeElements)

  behavior of "optionPartitioner"

  it should "put Some and None into different partitions (numbers)" in {
    testMaybeElements.map(LogInvalidProcessor.optionPartitioner[Test]) shouldBe Seq(1, 1, 0, 1)
  }

  behavior of "someMapper"

  it should "return only existing elements" in {
    val probe: TestProbe = TestProbe()
    val source: Source[(String, Option[Test]), NotUsed] = Source(testStingElements)
    source.via(LogInvalidProcessor.someMapper[Test]).runWith(Sink.seq).pipeTo(probe.ref)
    probe.expectMsg(3.seconds, testElements)
  }

  behavior of "noneStringMapper"

  it should "return the string from tuple pairs being none" in {
    val probe: TestProbe = TestProbe()
    val source: Source[(String, Option[Test]), NotUsed] = Source(testStingElements)
    source.via(LogInvalidProcessor.noneStringMapper[Test]).runWith(Sink.seq).pipeTo(probe.ref)
    probe.expectMsg(3.seconds, Seq("-1"))
  }

  behavior of "splitOffBadStringAndLog"

  it should "fill sink with successfully parsed entries" in {
    val source: Source[(String, Option[Test]), NotUsed] = Source(testStingElements)
    val sink: Sink[Test, Future[Seq[Test]]] = Sink.seq[Test]

    val result: Future[Seq[Test]] =
      new LogInvalidProcessor[Test](source = source, successfulSink = sink)
        .splitOffBadStringAndLog
        .run()

    Await.result(result, 1.seconds) shouldBe testElements
  }
}
