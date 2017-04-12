import com.rabbitmq.client._
import java.io.{BufferedReader, IOException, InputStreamReader}

import Technician.channel
import com.rabbitmq.client.AMQP.BasicProperties

import scala.util.Random

object Hospital{
  val f = new ConnectionFactory
  f.setHost("localhost")
  def connection: Connection = f.newConnection

  val random : Random = Random

  val infoChanel: String = "Admin.Info.Channel"
  val exchangeName: String = "hospital2"
  val exchangeType: BuiltinExchangeType = BuiltinExchangeType.TOPIC
  val surnameList: List[String] = List("Deep","Dubikowski","Kowal","Pitt","Nowak","Kowalski")
  val testType: List[String] = List("ankle","knee","elbow")

  def provideConsumer(function: (Channel, String, Envelope, BasicProperties) => Unit) = {
    new DefaultConsumer(channel) {
      @throws[IOException]
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        val message = new String(body, "UTF-8")
        message match {
          case m:String if m.startsWith("ADMININFO") => println(message)
          case _ => function(channel,message,envelope,properties);
        }
      }
    }
  }
}


object Doctor extends App {

  def doctorMethod(channel: Channel, msg: String, envelope: Envelope, properties: AMQP.BasicProperties) {
    println("Results: " + msg)
  }

  private def randomPatient: String = Hospital.surnameList(Hospital.random.nextInt(Hospital.surnameList.size))
  private def randomTest: String = Hospital.testType(Hospital.random.nextInt(Hospital.testType.size))

  val channel: Channel = Hospital.connection.createChannel()
  val callbackQueueName: String = channel.queueDeclare.getQueue

  channel.exchangeDeclare(Hospital.exchangeName,Hospital.exchangeType)
  channel.queueBind(callbackQueueName,Hospital.exchangeName,callbackQueueName)
  channel.queueBind(callbackQueueName,Hospital.exchangeName,Hospital.infoChanel)

  val props: BasicProperties  = new BasicProperties
    .Builder()
    .replyTo(callbackQueueName)
    .build()

  val callbackConsumer = Hospital.provideConsumer(doctorMethod)

  channel.basicConsume(callbackQueueName, true, callbackConsumer)

  val br: BufferedReader = new BufferedReader(new InputStreamReader(System.in))

  while (true) {
    val test = randomTest
    val message = test+":"+randomPatient
    br.readLine()
    channel.basicPublish(Hospital.exchangeName, test, props, message.getBytes("UTF-8"))
  }

}

object Technician extends App{
  val channel: Channel = Hospital.connection.createChannel()
  val technicianSkills: List[List[String]] = List(List("ankle", "knee"),List("ankle", "elbow"),List("knee", "elbow"))
  val skills: List[String] = technicianSkills(Hospital.random.nextInt(technicianSkills.size))
  channel.basicQos(1)

  def technicianMethod(channel: Channel, msg: String, envelope: Envelope, properties: AMQP.BasicProperties) {
     println("To test: " + msg)
     Thread.sleep(1000)
     channel.basicPublish(Hospital.exchangeName,properties.getReplyTo,null,msg.getBytes("UTF-8"))
     channel.basicAck(envelope.getDeliveryTag, false)
  }

  val consumer = Hospital.provideConsumer(technicianMethod)

  channel.exchangeDeclare(Hospital.exchangeName,Hospital.exchangeType)

  skills.foreach(e => {
    channel.queueDeclare(e,false,false,false,null)
    channel.queueBind(e,Hospital.exchangeName,e)
    channel.basicConsume(e, false, consumer)
  })

  val name = channel.queueDeclare.getQueue
  channel.queueBind(name,Hospital.exchangeName,Hospital.infoChanel)
  channel.basicConsume(name,false,consumer)
}

object Administrator extends App{
  val channel: Channel = Hospital.connection.createChannel()
  channel.exchangeDeclare(Hospital.exchangeName,Hospital.exchangeType)
  val queueNameToListen: String = channel.queueDeclare.getQueue
  channel.queueBind(queueNameToListen,Hospital.exchangeName,"*")

  val consumer = new DefaultConsumer(channel) {
    @throws[IOException]
    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
      val message = new String(body, "UTF-8")
      println(consumerTag+" :: "+message)
    }
  }
  channel.basicConsume(queueNameToListen, false, consumer)

  val br: BufferedReader = new BufferedReader(new InputStreamReader(System.in))
  while (true) {
    val message = "ADMININFO: "+br.readLine()
    channel.basicPublish(Hospital.exchangeName,Hospital.infoChanel , null, message.getBytes("UTF-8"))
  }

}
