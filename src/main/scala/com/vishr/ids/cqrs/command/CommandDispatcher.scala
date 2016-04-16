package com.vishr.ids.cqrs.command

import org.apache.kafka.clients.producer._
import net.liftweb.json._
import java.util.UUID
import java.util.Properties
import scala.concurrent.Future
import scala.concurrent.blocking
import scala.concurrent.ExecutionContext.Implicits.global
import org.slf4j.LoggerFactory
import java.net.InetAddress

trait CommandDispatcher {
   def send(key:String, message:String) : Future[String]
}

class KafkaCommandDispatcher (val topic:String, val hosts:String) extends CommandDispatcher { 

  val logger = LoggerFactory.getLogger(this.getClass)
  val host  = InetAddress.getLocalHost.getHostName
  val clientId = host + UUID.randomUUID().toString
  
  val props = new Properties()
  props.put("bootstrap.servers", hosts)
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("acks", "all")
  props.put("retries", "1")
  props.put("client.id",clientId.toString)
  logger.info("Attempting connection to kafka")
  val producer = new KafkaProducer[String, String](props)

  logger.info("connected to kafka")

  def send(key:String, message:String) : Future[String] = {
    val record = new ProducerRecord(topic, key,message)
    Future {
       blocking {
        val recordMetadata = producer.send(record).get
        logger.info("sent to kafka: "+message)
        // formulate orderKey using the offset of the record in kafka
        recordMetadata.partition() + "@" + recordMetadata.offset()
       }
    }
  }
}


object LocalKafkaCommandDispatcher extends KafkaCommandDispatcher("commands","localhost:9092")