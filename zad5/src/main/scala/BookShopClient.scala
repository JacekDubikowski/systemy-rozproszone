import java.io.File

import akka.actor.{Actor,  ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object BookShopClient {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseFile(new File("./client.conf"))
    val system = ActorSystem("client", config)
    val actor = system.actorOf(Props(new ClientActor), name="client")

    var x = true
    while (x) {
      val msg = StdIn.readLine()
      if (msg == "q") {
        x = false
      }
      actor.tell(Find(msg), actor)
    }
  }

}

class ClientActor extends Actor{
  val server: ActorSelection = context.actorSelection("akka.tcp://bookshop@127.0.0.1:2552/user/server")
  override def receive: Receive = {
    case Find(x) => server.tell(Stream(x), self)
    case Found(price) => println(price)
    case StreamChunk(chunk) => println(chunk)
    case x:String => println(x)
    case _ => ???
  }
}
