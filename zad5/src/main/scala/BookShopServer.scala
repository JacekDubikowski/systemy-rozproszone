import java.io.File

import akka.Done
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ThrottleMode}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.{Source => SourceIO}

object BookShopServer {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseFile(new File("./server.conf"))
    val system = ActorSystem("bookshop", config)
    val actor = system.actorOf(Props(new BookShopServer), name="server")
    while(true){}
  }
}

class BookShopServer extends Actor{
  val streamActor: ActorRef = context.actorOf(Props(new StreamActor))


  def find(title: String, sender: ActorRef): Unit = {
    val finderActor: ActorRef = context.actorOf(Props(new FinderActor(sender)))
    finderActor ! title
  }

  def order(title: String, sender: ActorRef): Unit = {
    val orderActor: ActorRef = context.actorOf(Props(new OrderActor(sender)))
    orderActor ! title
  }

  def stream(title: String, sender: ActorRef): Unit = {
    streamActor ! (title, sender)
  }

  override def receive: Receive = {
    case title: String if title.head == 'f' => find(title.tail, sender)
    case title: String if title.head == 'o' => order(title.tail, sender)
    case title: String if title.head == 's' => stream(title.tail, sender)
    case _ => println("Not known request from:" + sender);
  }

  class OrderActor(target: ActorRef) extends Actor{

    def handleOrder(title: String): Unit = {

    }

    override def receive: Receive = {
      case title: String =>
        handleOrder(title)
        context.stop(self)
      case _ => context.stop(self)
    }

  }

  class FinderActor(target: ActorRef) extends Actor{

    def handleFinding(title: String): Unit = {

    }

    override def receive: Receive = {
      case title: String =>
        handleFinding(title)
        context.stop(self)
      case _ => context.stop(self)
    }

  }

  class StreamActor extends Actor{
    implicit val materializer: ActorMaterializer = ActorMaterializer.create(context)

    def handleStream(title: String, target: ActorRef): Unit = {
      val sink: Sink[String, Future[Done]] = Sink.foreach(e => target ! e)
      try {
        Source.fromIterator(() => SourceIO.fromFile("./book/" + title + ".txt").getLines())
          .throttle(1, 1.second, 1, ThrottleMode.shaping)
          .runWith(sink).onComplete(_ => target ! true)
      } catch {
        case _: Throwable => target ! false
      }
    }

    override def receive: Receive = {
      case (title: String, target: ActorRef) =>
        handleStream(title,target)
      case _ => ???
    }
  }

}

sealed trait ServerMessage

case class Found(price: Double) extends ServerMessage
case class NotFound() extends ServerMessage
case class OrderConfirmation() extends ServerMessage
case class StreamChunk(chunk: String) extends ServerMessage