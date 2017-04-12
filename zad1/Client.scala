import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net._

import scala.io.StdIn

object Client {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      println("You are supposed to give your nickname!")
      System.exit(0)
    }
    else Client(args(0))
  }

  def apply(name: String): Unit = new Client(name).startClient()

  def port: Int = 12345
  def multicastPort: Int = 22222

  def localhost: String = "localhost"

  var portUDP = 22223

  val multicastAddress: InetAddress = InetAddress.getByName("230.1.1.1")
  val datagramMsg: String = "    ___  ________  ________  _______   ___  __       \n" + "   |\\  \\|\\   __  \\|\\   ____\\|\\  ___ \\ |\\  \\|\\  \\     \n   \\ \\  \\ \\  \\|\\  \\ \\  \\___|\\ \\   __/|\\ \\  \\/  /|_   \n __ \\ \\  \\ \\   __  \\ \\  \\    \\ \\  \\_|/_\\ \\   ___  \\  \n|\\  \\\\_\\  \\ \\  \\ \\  \\ \\  \\____\\ \\  \\_|\\ \\ \\  \\\\ \\  \\ \n\\ \\________\\ \\__\\ \\__\\ \\_______\\ \\_______\\ \\__\\\\ \\__\\\n \\|________|\\|__|\\|__|\\|_______|\\|_______|\\|__| \\|__|"
  val datagramMsgBytes: Array[Byte] = datagramMsg.getBytes()
}

class Client(name:String) {

  val id: String = name+"@"+(Math.random()*1000000).toInt

  def startClient(): Unit = {
    val socket: Socket = new Socket(Client.localhost, Client.port)
    val multicastSocket: MulticastSocket = prepareMulticastSocket()
    val udpSocket: DatagramSocket = new DatagramSocket()

    val out: PrintWriter = new PrintWriter(socket.getOutputStream, true)
    val in: BufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream))

    val datagramToSendMulticast: DatagramPacket = createDatagramToSendMulticast
    val datagramToSendUnicast: DatagramPacket = createDatagramToSendUnicast

    //console
    new Thread(new Runnable {
      override def run(): Unit = {
        out.println(id)
        out.println(udpSocket.getLocalPort.toString)
        while (true) {
          val msg = StdIn.readLine()
          if(msg.startsWith("-N")){
            multicastSocket.send(datagramToSendMulticast)
          }
          else if(msg.startsWith("-M")){
            udpSocket.send(datagramToSendUnicast)
          }
          else out.println(msg)
        }
      }
    }).start()

    //TCP receiver
    new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          val msg: String = in.readLine()
          if (msg == null) {
            println("Connection to server lost.")
            socket.close()
            System.exit(0)
          }
          println(msg)
        }
      }
    }).start()

    //Multicast receiver
    new Thread(new Runnable {
      override def run(): Unit = {
        val datagramToReceive: DatagramPacket = new DatagramPacket(new Array[Byte](1024), 1024)
        while (true) {
          multicastSocket.receive(datagramToReceive)
          val x = new String(datagramToReceive.getData).split("\\s",2)
          if(id != x(0)) println(x(0)+":\n" + x(1))
        }
      }
    }).start()

    //UDP receiver
    new Thread(new Runnable {
      override def run(): Unit = {
        val datagramToReceive2: DatagramPacket = new DatagramPacket(new Array[Byte](1024), 1024)
        while(true){
          udpSocket.receive(datagramToReceive2)
          println(new String(datagramToReceive2.getData))
        }
      }
    }).start()
  }

  private def createDatagramToSendUnicast = {
    val datagramToSend: DatagramPacket = createDatagram
    datagramToSend.setAddress(InetAddress.getByName(Client.localhost))
    datagramToSend.setPort(Client.port)
    datagramToSend
  }

  private def createDatagramToSendMulticast = {
    val datagramToSend: DatagramPacket = createDatagram
    datagramToSend.setAddress(Client.multicastAddress)
    datagramToSend.setPort(Client.multicastPort)
    datagramToSend
  }

  private def createDatagram = {
    val toSend = (id + "\n").getBytes() ++ Client.datagramMsgBytes
    val datagramToSend = new DatagramPacket(toSend, toSend.length)
    datagramToSend
  }

  private def prepareMulticastSocket() = {
    val socket = new MulticastSocket(Client.multicastPort)
    socket.joinGroup(Client.multicastAddress)
    socket
  }
}