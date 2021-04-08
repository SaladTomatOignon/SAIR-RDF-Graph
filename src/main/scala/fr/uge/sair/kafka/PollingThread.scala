package fr.uge.sair.kafka

import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.errors.InterruptException

import java.time.Duration
import java.util.function.Consumer

/**
 * Thread that makes a consumer polling indefinitely
 * until it gets interrupted.
 *
 * @param consumer The consumer to poll
 * @tparam K The key type of a record
 * @tparam V The value type of a record
 */
class PollingThread[K, V](consumer: KafkaConsumer[K, V]) extends Thread {
  /**
   * The maximum time to block
   * for the consumer when polling
   * (in milliseconds)
   */
  val pollingTimeOut: Long = 500

  /**
   * The function that will consume every polled record
   */
  var recordConsumer: Option[Consumer[ConsumerRecord[K, V]]] = None

  override def run(): Unit = {
    var polling = true

    while (polling) {
      try {
        val records = consumer.poll(Duration.ofMillis(pollingTimeOut))
        if (recordConsumer.nonEmpty) {
          records.forEach(record => recordConsumer.get.accept(record))
        }
      } catch {
        case _: InterruptException =>
          polling = false
      }
    }
  }
}
