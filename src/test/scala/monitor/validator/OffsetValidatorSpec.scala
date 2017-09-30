package monitor
package validator

import fixture.GroupOffsetFixture
import model.GroupOffset
import org.apache.kafka.common.TopicPartition
import org.scalatest.prop.TableDrivenPropertyChecks._

import scala.util.{Success, Try}


class OffsetValidatorSpec extends BaseTest with GroupOffsetFixture {

//  private val group = "g1"
//  private val topicPartition = new TopicPartition("t1", 1)
//  private val baseGroupOffset = GroupOffset(group, topicPartition, 0, 0)

  private val offsetValidator = new OffsetValidator[Try]()

  it should "return Success[model.Success]" in withGroupOffset { (_, _, baseGroupOffset) =>
    val cases = Table (
      "group topic offset",
      baseGroupOffset,
      baseGroupOffset.copy(committedOffset = 15, endOffset = 15)
    )

    forAll(cases) { t =>
      offsetValidator.validate(t) shouldBe Success(model.Success)
    }
  }

  it should "return Success[model.Error]" in withGroupOffset { (group, topicPartition, baseGroupOffset) =>
    val cases = Table (
      "group topic offset",
      baseGroupOffset.copy(committedOffset = 0, endOffset = 1),
      baseGroupOffset.copy(committedOffset = 10, endOffset = 13)
    )

    forAll(cases) { t =>
      offsetValidator.validate(t) shouldBe
        Success(model.Error(group, topicPartition.topic(), topicPartition.partition(), t.committedOffset, t.endOffset, t.endOffset - t.committedOffset))
    }
  }

  it should "return Failure[Exception]" in withGroupOffset { (_, _, baseGroupOffset) =>
    val cases = Table (
      "group topic offset",
      baseGroupOffset.copy(committedOffset = 1, endOffset = 0),
      baseGroupOffset.copy(committedOffset = 0, endOffset = -1),
      baseGroupOffset.copy(committedOffset = -1, endOffset = 0)
    )

    forAll(cases) { t =>
      offsetValidator.validate(t) shouldBe 'failure
    }
  }
}
