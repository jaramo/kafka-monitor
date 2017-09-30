package monitor

import scala.concurrent.{ExecutionContext, Future}
import scalaz.MonadError

package object implicits {

  implicit lazy val ex: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  implicit lazy val futureMonadError: MonadError[Future, Throwable] = scalaz.std.scalaFuture.futureInstance

}