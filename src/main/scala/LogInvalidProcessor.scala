import LogInvalidProcessor._
import ProtocolParser.userParser
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ClosedShape, UniformFanOutShape}
import akka.stream.scaladsl.{Flow, GraphDSL, Partition, RunnableGraph, Sink, Source}
import akka.util.ByteString
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

class LogInvalidProcessor[T <: Product]
(
  source: Source[ByteString, NotUsed],
  successfulSink: Sink[T, Future[Seq[T]]]
)(implicit system: ActorSystem, ec: ExecutionContext) {

  final lazy val logger: Logger = LoggerFactory.getLogger(getClass.getSimpleName)

  final val logSink: Sink[String, Future[Done]] =
    Sink.foreach((invalidString: String) => logger.warn(s"found invalid message string: '$invalidString'"))

  val splitOffBadStringAndLog: RunnableGraph[Future[Seq[T]]] = {
    RunnableGraph
      .fromGraph(GraphDSL.createGraph(successfulSink) { implicit builder =>
        outputSink =>
          import GraphDSL.Implicits._
          val partition: UniformFanOutShape[(String, Option[T]), (String, Option[T])] =
            builder.add(Partition[(String, Option[T])](outputPorts = 2, element => optionPartitioner[T](element._2)))
          source.via(userParser) ~> partition.in
          partition.out(0) ~> noneStringMapper[T] ~> logSink
          partition.out(1) ~> someMapper[T] ~> outputSink
          ClosedShape
      })
  }

}

object LogInvalidProcessor {

  def optionPartitioner[T <: Product]: Option[T] => Int =
    element => element.map(_ => 1).getOrElse(0)

  def someMapper[T <: Product]: Flow[(String, Option[T]), T, NotUsed] =
    Flow[(String, Option[T])].mapConcat(_._2.toList)

  def noneStringMapper[T <: Product]: Flow[(String, Option[T]), String, NotUsed] =
    Flow[(String, Option[T])].filter(_._2.isEmpty).map(_._1)
}
