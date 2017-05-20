import java.io.File

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object BookShopClient {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseFile(new File("./client.conf"))
    val system = ActorSystem("client", config)
    val actor = system.actorOf(Props(new ClientActor), name="client")

    while (true) {
      println("[find/order/stream]: <title>")
      val msg = StdIn.readLine()
      if (msg == "q") {
        system.terminate()
      }
      else if (msg.startsWith("find: ")){
        actor ! Find(msg.drop(6).trim)
      }
      else if (msg.startsWith("order: ")){
        actor ! Order(msg.drop(7).trim)
      }
      else if (msg.startsWith("stream: ")){
        actor ! Stream(msg.drop(8).trim)
      }
      else {
        println("Unknown command")
      }
    }
  }

}

object ClientActor{
  val unexpectedMessage = "Unexpected msg received."
}

class ClientActor extends Actor{
  val server: ActorSelection = context.actorSelection("akka.tcp://bookshop@127.0.0.1:2552/user/server")

  def handleFind(msg: Find): Unit = {
    val findActor: ActorRef = context.actorOf(Props(new FindActor))
    findActor ! msg
  }

  def handleOrder(msg: Order): Unit = {
    val orderActor: ActorRef = context.actorOf(Props(new OrderActor))
    orderActor ! msg
  }

  def handleStream(msg: Stream): Unit = {
    val streamActor: ActorRef = context.actorOf(Props(new StreamActor))
    streamActor ! msg
  }

  override def receive: Receive = {
    case x: Find   => handleFind(x)
    case x: Order  => handleOrder(x)
    case x: Stream => handleStream(x)
    case _         => println(ClientActor.unexpectedMessage)
  }

  class FindActor extends Actor {
    var title: String = _
    override def receive: Receive = {
      case Find(x)          =>
        title = x
        server ! "f" + x
      case x: Boolean if !x =>
        println(title + " not found")
        context.stop(self)
      case x: Double        =>
        println(title + " costs " + x)
        context.stop(self)
      case _                =>
        println(ClientActor.unexpectedMessage)
        context.stop(self)
    }
  }

  class OrderActor extends Actor {
    var title: String = _
    override def receive: Receive = {
      case Order(x)         =>
        title = x
        server ! "o" + x
      case x: Boolean if !x =>
        println(title + " not ordered")
        context.stop(self)
      case x: Boolean if  x =>
        println(title + " ordered")
        context.stop(self)
      case _                =>
        println(ClientActor.unexpectedMessage)
        context.stop(self)
    }
  }

  class StreamActor extends Actor {
    var title: String = _
    override def receive: Receive = {
      case Stream(x) =>
        title = x
        server ! "s" + x
      case x: Boolean if !x =>
        println(title + " not available to stream")
        context.stop(self)
      case x: Boolean if  x =>
        println("Stream finished")
        context.stop(self)
      case x: String        => println(x)
      case _                =>
        println(ClientActor.unexpectedMessage)
        context.stop(self)
    }
  }
}

sealed trait ClientMessage
case class Find(title: String) extends ClientMessage
case class Order(title: String) extends ClientMessage
case class Stream(title: String) extends ClientMessage