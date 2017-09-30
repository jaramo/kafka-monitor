package monitor
package source

trait Source[T] {
  def start(): Unit
  def stop(): Unit
  def produce: Iterator[T]
}
