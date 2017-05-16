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
    case Find(title) => find(title, sender)
    case Order(title) => order(title, sender)
    case Stream(title) => stream(title, sender)
    case _ => println(self.path);
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

    def handleStream(title: String, target: ActorRef): Unit = {
      implicit val materializer = ActorMaterializer.create(context)
      val sink: Sink[String, Future[Done]] = Sink.foreach(e => target ! e)
      val source = Source.fromIterator(() => SourceIO.fromFile("./book/80day10.txt").getLines())
        .throttle(1,1.second,1,ThrottleMode.shaping)
        .runWith(sink)
//        .onComplete(e => context.stop(self))
    }

    override def receive: Receive = {
      case (title: String, target: ActorRef) =>
        handleStream(title,target)
      case _ => ???
    }
  }

}