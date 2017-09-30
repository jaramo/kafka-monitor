package monitor
package fixture

import java.util.concurrent.atomic.AtomicInteger

import model.GroupOffset
import org.apache.kafka.common.TopicPartition

import scala.collection.AbstractIterator

trait GroupOffsetFixture {

  private val group = "g1"
  private val topicPartition = new TopicPartition("t1", 1)
  private val groupOffset = GroupOffset(group, topicPartition, 0, 0)


  protected def withGroupOffset(test: (String, TopicPartition, GroupOffset) => Any): Any = {
    test(group, topicPartition, groupOffset)
  }

  protected def withGroupOffsetIterator(iterations: Int)(test: Iterator[GroupOffset] => Any): Any = {
    val iterator = new AbstractIterator[GroupOffset] {
      private val counter = new AtomicInteger(0)
      override def hasNext: Boolean = counter.getAndIncrement() < iterations
      override def next(): GroupOffset =  groupOffset
    }

    test(iterator)
  }
}

