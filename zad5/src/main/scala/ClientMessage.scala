

sealed trait ClientMessage

case class Find(title: String) extends ClientMessage
case class Order(title: String) extends ClientMessage
case class Stream(title: String) extends ClientMessage

sealed trait ServerMessage

case class Found(price: Double) extends ServerMessage
case class NotFound() extends ServerMessage
case class OrderConfirmation() extends ServerMessage
case class StreamChunk(chuck: String) extends ServerMessage