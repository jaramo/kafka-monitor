package monitor
package notifier

import java.io.PrintStream

import model._

import scalaz.MonadError

class PrintStreamNotifier[F[_]](output: PrintStream)
  (implicit val ME: MonadError[F, Throwable]) extends Notifier[F] {

  override def notify(r: ValidationResult): F[Unit] =
    ME.point(output.println(r))
}
