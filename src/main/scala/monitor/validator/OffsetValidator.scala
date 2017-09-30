package monitor
package validator

import model._

import scalaz.MonadError

class OffsetValidator[F[_]]()(implicit val ME: MonadError[F, Throwable]) {

  def validate(x: GroupOffset): F[ValidationResult] =
    ME.point {
      x.committedOffset.ensuring(_ >= 0, "committed offset can't be negative")

      x.endOffset.ensuring(_ >= 0, "end offset can't be negative")
      x.endOffset.ensuring(_ >= x.committedOffset, "committed offset can't be greater than end offset")

      val lag = x.endOffset - x.committedOffset
      if (lag > 0)
        Error(x.group, x.topicPartition.topic(), x.topicPartition.partition(), x.committedOffset, x.endOffset, lag)
      else
        Success
    }

}
