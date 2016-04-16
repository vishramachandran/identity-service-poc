package com.vishr.ids.cqrs.processor

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.softwaremill.react.kafka.KafkaMessages._
import akka.stream.scaladsl.Source
import com.softwaremill.react.kafka.{ ConsumerProperties, ReactiveKafka }
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import net.liftweb.json._
import net.liftweb.json.Serialization.read
import com.vishr.ids.db.model.Command
import com.vishr.ids.common.LiftFormats
import com.vishr.ids.common.DependencyInjector
import org.slf4j.LoggerFactory

object ProcessorMain extends App {

  val logger = LoggerFactory.getLogger(this.getClass)
  implicit val formats = LiftFormats.formats
  implicit val actorSystem = ActorSystem("ReactiveKafka")
  implicit val materializer = ActorMaterializer()
  def commandProcessor = DependencyInjector.commandProcessor
  
  
  val kafka = new ReactiveKafka()
  val consumerProperties = ConsumerProperties(
    bootstrapServers = "localhost:9092",
    topic = "commands",
    groupId = "commandsProcessor7",
    valueDeserializer = new StringDeserializer())
    .commitInterval(5 seconds) // flush interval
    .readFromEndOfStream()
    
  val consumerWithOffsetSink = kafka.consumeWithOffsetSink(consumerProperties)
  Source.fromPublisher(consumerWithOffsetSink.publisher)
    .map(i => processMessage[ConsumerRecord[Array[Byte],String]](i)) // your message processing
    .to(consumerWithOffsetSink.offsetCommitSink) // stream back for commit
    .run()

  def processMessage[T](msg: ConsumerRecord[Array[Byte],String]) = {
    logger.info("Received message: " + msg.value)
    
    try {
      val command = read[Command](msg.value())
      commandProcessor.processCommand(command)
    } catch {
      case e:Exception => logger.error("Got exception while processing: "+msg.value, e)
    }
    msg
  }
    
    
    
}