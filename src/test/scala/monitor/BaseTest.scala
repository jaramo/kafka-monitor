package monitor

import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import org.scalatest._

import scala.util.{Failure, Try}
import scalaz.MonadError

abstract class BaseTest extends FlatSpec
  with Matchers
  with BeforeAndAfterAll
  with MockitoSugar
  with BeforeAndAfter
  with OptionValues
  with Eventually
  with Inspectors {

  implicit object TryMonadError extends MonadError[Try, Throwable] {

    override def raiseError[A](e: Throwable): Try[A] =
      Failure(e)

    override def handleError[A](fa: Try[A])(f: (Throwable) => Try[A]): Try[A] =
      fa.recoverWith(PartialFunction(f))

    override def point[A](a: => A): Try[A] =
      Try(a)

    override def bind[A, B](fa: Try[A])(f: (A) => Try[B]): Try[B] =
      fa.flatMap(f)
  }

}