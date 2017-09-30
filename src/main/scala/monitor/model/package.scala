package monitor

import org.apache.kafka.common.TopicPartition

package object model {

  case class GroupOffset(
    group: String,
    topicPartition: TopicPartition,
    committedOffset: Long,
    endOffset: Long)

  sealed trait ValidationResult {
    def isSuccess: Boolean
    def isError: Boolean = !isSuccess
  }
  case object Success extends ValidationResult {
    override def isSuccess: Boolean = true
  }

  case class Error(
    group: String,
    topic: String,
    partition: Int,
    committedOffset: Long,
    endOffset: Long,
    lag: Long
  ) extends ValidationResult {

    override def isSuccess: Boolean = false

    override def toString: String =
      s"Error: group[$group] - topic[$topic-$partition] - committed[$committedOffset] - end[$endOffset] - lag[$lag]"
  }

  object config {
    case class Config(bootstrapServers: String, topics: Set[String] = Set.empty)
  }

}
