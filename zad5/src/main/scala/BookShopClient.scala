import java.io.File

import akka.actor.{Actor,  ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object BookShopClient {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseFile(new File("./remote_app2.conf"))
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
  val system: ActorSelection = context.actorSelection("akka.tcp://bookshop@127.0.0.1:2552/user/server")
  override def receive: Receive = {
    case x:Find => system.tell(x, self)
    case Found(price) => println(price)
    case _ => ???
  }
}
