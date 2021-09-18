import ProtocolParser.PROTOCOL_MESSAGE_SEPARATOR
import akka.NotUsed
import akka.stream.scaladsl.{BidiFlow, Flow, Framing, Sink}
import akka.util.ByteString
import akka.util.ByteString.UTF_8

object SerDe {

  final val lineSplitter: Flow[ByteString, ByteString, _] =
    Framing.delimiter(ByteString(PROTOCOL_MESSAGE_SEPARATOR), maximumFrameLength = 256, allowTruncation = true)

  final val mergeMessages: Seq[ByteString] => ByteString = list => ByteString(list.mkString(PROTOCOL_MESSAGE_SEPARATOR))
  final val lineMerger: Flow[Seq[ByteString], ByteString, NotUsed] = Flow.fromFunction(mergeMessages)

  final type Message = String

  final val deserialize: ByteString => Message = _.utf8String
  final val serialize: Message => ByteString = message => ByteString(message.getBytes(UTF_8))

  final val incoming: Flow[ByteString, Message, _] = Flow.fromFunction(deserialize)
  final val outgoing: Flow[Message, ByteString, _] = Flow.fromFunction(serialize)

  final val serDeFlow: BidiFlow[ByteString, Message, Message, ByteString, NotUsed] = BidiFlow.fromFlows(incoming, outgoing)

  final val logSink: Sink[Message, _] = Sink.foreach(println)

}
