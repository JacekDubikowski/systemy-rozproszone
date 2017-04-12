import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net._
import java.util.concurrent.{ExecutorService, Executors}

import scala.util.control.Breaks._
import scala.collection.concurrent.TrieMap

object Server {
  val portNumber: Int = 12345
  val users: TrieMap[String, Handler] = new TrieMap[String, Handler]()
  val pool: ExecutorService = Executors.newFixedThreadPool(20)

  def sendMessage(user: Handler, msg: String): Unit = user.writer.println(msg)
  def apply(): Unit = new Server().startServer()
  def main(args: Array[String]): Unit = Server()
}

class Server{
  def startServer(): Unit = {
    try{
      val socket = new ServerSocket(Server.portNumber)
      val udpSocket = new DatagramSocket(Server.portNumber)
      val datagramToReceive = new DatagramPacket(new Array[Byte](1024), 1024)

      Server.pool.execute(new Thread(new Runnable {
        override def run(): Unit = {
          while(true){
            udpSocket.receive(datagramToReceive)
            val x = new String(datagramToReceive.getData).split("\\s",2)
            val msg = (x(0)+":\n"+x(1)).getBytes()
            val datagramToSend = new DatagramPacket(msg, msg.length)
            Server.users.filter(p => p._1!=x(0)).foreach(p=>{
              val user = p._2
              datagramToSend.setAddress(user.getSocket.getInetAddress)
              datagramToSend.setPort(user.portUDP)
              udpSocket.send(datagramToSend)
            })
          }
        }
      }))

      while(true){
        Server.pool.execute(new Thread(Handler(socket.accept())))
      }
    } catch {
      case _ : java.net.BindException => println("Port taken")
      case _ : Throwable => println("Server cannot work")
    }
  }
}

object Handler {
   def apply(socket: Socket): Handler = {
     val in: BufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream))
     val name = in.readLine()
     val portUDP = Integer.valueOf(in.readLine())
     val out =  new PrintWriter(socket.getOutputStream, true)
     val user = new Handler(socket,name, out, in, portUDP)
     for((_,u) <- Server.users) Server.sendMessage(u,"User " + name + " joined chat.")
     Server.users.put(name,user)
     user
   }
}

class Handler(socket: Socket, user: String, out: PrintWriter, in: BufferedReader, portUdp: Int) extends Runnable{

  def writer: PrintWriter = out

  def portUDP: Int = portUdp

  def name: String = user

  def getSocket: Socket = socket

  override def run(): Unit = {
    breakable {
      while (true) {
        val msg = in.readLine()
        if (msg == null) {
          for ((_, u) <- Server.users; if u != this) Server.sendMessage(u, "user "+ user + " left chat.")
          Server.users.remove(user)
          break
        }
        val readyMsg = user + ": " + msg
        for ((_, u) <- Server.users; if u != this) Server.sendMessage(u, readyMsg)
      }
    }
  }
}