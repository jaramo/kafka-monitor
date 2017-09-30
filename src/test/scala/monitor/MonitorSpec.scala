package monitor

import monitor.fixture.GroupOffsetFixture
import monitor.model.GroupOffset
import monitor.notifier.Notifier
import monitor.source.Source
import monitor.validator.OffsetValidator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class MonitorSpec extends BaseTest with GroupOffsetFixture {

  private val validator = mock[OffsetValidator[Try]]
  private val notifier =  mock[Notifier[Try]]
  private val source = mock[Source[GroupOffset]]

  private val monitor = new Monitor[Try](validator, notifier, List(source))

  before {
    reset(validator, notifier, source)
  }

  it should "should do nothing when the iterator is empty" in {
    when(source.produce).thenReturn(Iterator.empty)
    monitor.start()
    monitor.stop()

    verifyZeroInteractions(validator)
    verifyZeroInteractions(notifier)
    verify(source, times(1)).stop()
  }


  it should "should not notify" in withGroupOffsetIterator(1) { iterator =>
    when(source.produce)
      .thenReturn(iterator)

    when(validator.validate(any(classOf[GroupOffset])))
      .thenReturn(Try(model.Success))

    monitor.start()
    monitor.stop()

    verifyZeroInteractions(notifier)
    verify(source, times(1)).stop()
  }

  it should "should notify" in withGroupOffsetIterator(1) { iterator =>
    val error = model.Error(
      group = "g1",
      topic = "t2",
      partition = 0,
      committedOffset = 5,
      endOffset = 13,
      lag = 8)

    when(source.produce)
      .thenReturn(iterator)

    when(validator.validate(any(classOf[GroupOffset])))
      .thenReturn(Try(error))

    monitor.start()
    monitor.stop()

    verify(notifier, times(1)).notify(error)
    verify(source, times(1)).stop()
  }

}
