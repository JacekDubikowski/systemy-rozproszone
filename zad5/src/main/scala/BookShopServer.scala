import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import java.io.File

import com.typesafe.config.ConfigFactory

object BookShopServer {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseFile(new File("./remote_app.conf"))
    val system = ActorSystem("bookshop", config)
    val actor = system.actorOf(Props(new BookShopServer), name="server")
    while(true){}
  }
}

class BookShopServer extends Actor{

  def find(title: String, sender: ActorRef): Unit = {
    val finderActor: ActorRef = context.actorOf(Props(new FinderActor(sender)))
    finderActor ! title
  }

  def order(title: String, sender: ActorRef): Unit = {
    val orderActor: ActorRef = context.actorOf(Props(new OrderActor(sender)))
    orderActor ! title
  }

  def stream(title: String, sender: ActorRef): Unit = {
    val streamActor: ActorRef = context.actorOf(Props(new StreamActor(sender)))
    streamActor ! title
  }

  override def receive: Receive = {
    case Find(title) => find(title, sender)
    case Order(title) => order(title, sender)
    case Stream(title) => order(title, sender)
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

  class StreamActor(target: ActorRef) extends Actor{

    def handleStream(title: String): Unit = {

    }

    override def receive: Receive = {
      case title: String =>
        handleStream(title)
        context.stop(self)
      case _ => context.stop(self)
    }
  }

}