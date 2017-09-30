package monitor
package notifier

import java.io.PrintStream

import scala.util.Try
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any

class PrintStreamNotifierSpec extends BaseTest {

  private val printStream = mock[PrintStream]

  private val notifier = new PrintStreamNotifier[Try](printStream)

  before {
    reset(printStream)
  }

  it should "succeed" in {
    doNothing()
      .when(printStream).println(any(classOf[model.ValidationResult]))

    notifier.notify(model.Success) shouldBe 'success
  }

  it should "fail" in {
    when(printStream.println(any(classOf[model.ValidationResult])))
        .thenThrow(new RuntimeException("PrintStream not available"))

    notifier.notify(model.Success) shouldBe 'failure
  }

}
