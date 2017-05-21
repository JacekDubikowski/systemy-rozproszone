import java.io.{File, FileNotFoundException, PrintWriter}
import java.nio.charset.Charset
import java.nio.file.{Files, Paths, StandardOpenOption}

import akka.Done
import akka.actor.SupervisorStrategy.{Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision, ThrottleMode}
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

  override def receive: Receive = {
    case title: String if title.head == 'f' => find(title.tail, sender)
    case title: String if title.head == 'o' => order(title.tail, sender)
    case title: String if title.head == 's' => stream(title.tail, sender)
    case _                                  =>
      println("Not known request from " + sender)
      sender ! false
  }

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Throwable                => Resume
    }

  def writeOrder(order: String): Unit ={
    synchronized{
      val writer = new PrintWriter(
        Files.newBufferedWriter(
          Paths.get("./orders.txt"),
          Charset.defaultCharset(),
          StandardOpenOption.CREATE,
          StandardOpenOption.WRITE,
          StandardOpenOption.APPEND
        )
      )
      writer.println(order)
      writer.close()
    }
  }

  def find(title: String, sender: ActorRef): Unit = {
    val finderActor: ActorRef = context.actorOf(Props(new FinderActor(sender)))
    finderActor.forward(title)(context)
  }

  def order(title: String, sender: ActorRef): Unit = {
    val orderActor: ActorRef = context.actorOf(Props(new OrderActor()))
    orderActor.forward(title)(context)
  }

  def stream(title: String, sender: ActorRef): Unit = {
    streamActor ! (title, sender)
  }

  class OrderActor extends Actor{

    def handleOrder(title: String): Unit = {
      try{
        writeOrder(title)
        sender ! true
      }
      catch {
        case _:Throwable => sender ! false
      } finally {
        context.stop(self)
      }
    }

    override def receive: Receive = {
      case title: String =>
        handleOrder(title)
        context.stop(self)
      case _ => context.stop(self)
    }

  }

  class FinderActor(target: ActorRef) extends Actor{
    private val search1 = context.actorOf(Props(new DatabaseSearchingActor("database1.txt")))
    private val search2 = context.actorOf(Props(new DatabaseSearchingActor("database2.txt")))

    override val supervisorStrategy: OneForOneStrategy =
      OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: FileNotFoundException    =>
          target ! false
          Stop
        case _: NumberFormatException    => Resume
        case _: Throwable                => Stop
      }

    def handleFinding(title: String): Unit = {
      search1.forward(title)(context)
      search2.forward(title)(context)
    }

    override def receive: Receive = {
      case title: String         =>
        handleFinding(title)
      case _                     => context.stop(self)
    }

    class DatabaseSearchingActor(filename: String) extends Actor{
      override def receive: Receive = {
        case x:String => findInDatabase(x)
        case _ => context.stop(self)
      }
      def findInDatabase(title: String): Unit = {
        SourceIO.fromFile(filename).getLines().foreach(line =>
          if(line.toLowerCase.contains(title.toLowerCase)) sender ! line
            .trim
            .reverse
            .takeWhile(x => !x.isWhitespace)
            .reverse
            .toDouble
        )
        sender ! false
        context.stop(self)
      }
    }
  }

  class StreamActor extends Actor{

    private val decider: Supervision.Decider = {
      case _: Throwable => Supervision.Stop
    }

    implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system).withSupervisionStrategy(decider))

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
      case _ => println("Unexpected message.")
    }
  }

}