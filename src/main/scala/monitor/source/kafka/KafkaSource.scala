package monitor
package source
package kafka

import java.util.concurrent.LinkedBlockingQueue
import java.util.{Collections, Properties}

import implicits.ex
import model.GroupOffset
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer

import scala.collection.AbstractIterator
import scala.collection.JavaConverters._
import scala.concurrent.duration.TimeUnit
import scala.concurrent.{Future, duration}

class KafkaSource(
  bootstrapServer: String,
  group: String,
  topicPartition: TopicPartition,
) extends Source[GroupOffset] {


  private val queue = new LinkedBlockingQueue[GroupOffset](1)

  private val deserializer = classOf[StringDeserializer].getName
  private var running: Boolean = false

  private def createConsumer(): KafkaConsumer[_, _] = {
    val properties = new Properties()
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, group)
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, deserializer)
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer)

    val consumer = new KafkaConsumer(properties)
    consumer.assign(Collections.singletonList(topicPartition))
    consumer
  }

  private def init(timeout: Long, timeUnit: TimeUnit): Unit = {
    Future {
      while (true) {
        val consumer = createConsumer()
        val committedOffset = consumer.committed(topicPartition).offset()
        val endOffset = consumer.endOffsets(Collections.singletonList(topicPartition)).values().asScala.head
        consumer.close()

        queue.put(GroupOffset(group, topicPartition, committedOffset, endOffset))

        Thread.sleep(timeUnit.toMillis(timeout))
      }
    }
  }

  def isRunning: Boolean = running

  def start(timeout: Long, timeUnit: TimeUnit): Unit = {
    if (!isRunning) {
      init(timeout, timeUnit)
      running = true
    }
  }

  override def start(): Unit = {
    start(30L, duration.SECONDS)
  }

  override def stop(): Unit = {
    if (isRunning) {
      queue.clear()
      running = false
    }
  }

  override def produce: Iterator[GroupOffset] =
    new AbstractIterator[GroupOffset] {
      override def hasNext: Boolean = running

      override def next(): GroupOffset = queue.take()
    }

}
