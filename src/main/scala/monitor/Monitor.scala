package monitor

import validator._
import notifier._
import source._
import model._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scalaz.MonadError
import scalaz.syntax.monadError._


class Monitor[F[_]](
  validator: OffsetValidator[F],
  notifier: Notifier[F],
  sources: List[Source[GroupOffset]]
)(implicit val ME: MonadError[F, Throwable], ex: ExecutionContext) extends LazyLogging {

  def start(): Unit = {
    Await.ready(Future.sequence(sources.map(consume)), Duration.Inf)
  }

  def stop(): Unit = {
    sources.foreach(_.stop())
  }

  private def consume(source: Source[GroupOffset]): Future[Unit] =
    Future {
      source.start()
      source.produce.foreach { groupOffset =>
        validator.validate(groupOffset)
          .flatMap { t =>
            if (t.isError) notifier.notify(t)
            else ().point
          }
          .handleError { error =>
            logger.error(s"Error processing $groupOffset", error)
            error.raiseError
          }
      }
    }

}

