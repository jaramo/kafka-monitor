package monitor

import com.typesafe.scalalogging.LazyLogging
import kafka.admin.AdminClient
import monitor.implicits._
import monitor.model.config.Config
import monitor.notifier.PrintStreamNotifier
import monitor.source.kafka.KafkaSource
import monitor.validator.OffsetValidator
import pureconfig._

import scala.concurrent.Future

object MonitorApp extends App with LazyLogging {

  val conf: Config = loadConfigOrThrow[Config]

  val adminClient = AdminClient.createSimplePlaintext(conf.bootstrapServers)

  val sources = adminClient.listAllConsumerGroupsFlattened.flatMap { groupView =>
    adminClient.listGroupOffsets(groupView.groupId)
      .filter {
        case (t, _) if conf.topics.nonEmpty => conf.topics.contains(t.topic())
        case _ => true
      }
      .map { t =>
      new KafkaSource(conf.bootstrapServers, groupView.groupId, t._1)
    }
  }

  if (sources.isEmpty) {
    val msg = s"unable to initialize sources for config: $conf"
    logger.error(msg)
    sys.error(msg)
  }

  val monitor = new Monitor[Future](
    new OffsetValidator[Future](),
    new PrintStreamNotifier[Future](System.out),
    sources)

  logger.info("Starting monitor")
  monitor.start()

  sys.addShutdownHook {
    logger.info("Stopping monitor")
    monitor.stop()
    logger.info("Monitor stopped")
  }
}




