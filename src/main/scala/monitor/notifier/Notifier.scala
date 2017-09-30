package monitor
package notifier

import model.ValidationResult

trait Notifier[F[_]] {

 def notify(r: ValidationResult): F[Unit]

}
