package fr.uge.sair.kafka

import fr.uge.sair.data.Vaccine
import fr.uge.sair.kafka.streams.{LUBM1stream, SideEffectStream}
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import java.util.Properties
import java.util.concurrent.{ExecutionException, TimeUnit}
import java.util.function.Consumer
import scala.collection.JavaConverters.{asScalaSet, seqAsJavaListConverter}

object Kafka {
  val appName: String = "LUBM-Vaccinations"
  private var kafkaStreams: Option[KafkaStreams] = None
  private var admin: Option[AdminClient] = None
  private var consumersGroup: Option[Array[KafkaConsumer[String, String]]] = None
  private var consumersGroupThreads: Option[Array[PollingThread[String, String]]] = None
  var recordConsumer: Option[Consumer[ConsumerRecord[String, String]]] = None
  private[this] var _producer: Option[KafkaProducer[String, String]] = None

  private def properties: Properties = {
    val props = new Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, appName)
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "Vaccines-consumers")

    props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "fr.uge.sair.kafka.VaccinePartitioner")

    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")

    props
  }

  /**
   * Creates, if not exist, the 4 topics of this program,
   * with as many partitions as there are vaccines, for each of them.
   */
  private def initTopics(): Unit = {
    val nbPartitions = Vaccine.values.size
    val topics = List(
      new NewTopic(LUBM1stream.inputTopicName, nbPartitions, 1.toShort),
      new NewTopic(LUBM1stream.outputTopicName, nbPartitions, 1.toShort),
      new NewTopic(SideEffectStream.inputTopicName, nbPartitions, 1.toShort),
      new NewTopic(SideEffectStream.outputTopicName, nbPartitions, 1.toShort)
    )

    var existingsTopics = this.admin.get.listTopics().names().get()
    if (topics.map(topic => topic.name()).exists(topic => !existingsTopics.contains(topic))) {
      try {
        this.admin.get.createTopics(topics.asJava).all().get()
      } catch {
        case _: ExecutionException =>
          // Removing all existing topics related to this app, to have a clean workspace
          existingsTopics = this.admin.get.listTopics().names().get()
          val topicsToRemove = asScalaSet(existingsTopics).filter(existingTopic =>
            topics.exists(topic => existingTopic.equals(topic.name()) || existingTopic.contains(appName))
          ).toList.asJava
          this.admin.get.deleteTopics(topicsToRemove).all().get()

          // Sleep time because this deleteTopics() method does not fully perform its action despite the get() method call
          TimeUnit.MILLISECONDS.sleep(1000)
          this.admin.get.createTopics(topics.asJava).all().get()
      }
    }
  }

  def producer: KafkaProducer[String, String] = _producer.get

  private def initProducers(): Unit = {
    this._producer = Some(new KafkaProducer[String, String](properties))
  }

  def setConsumingFunction(consumer: Consumer[ConsumerRecord[String, String]]): Unit = {
    this.consumersGroupThreads.get.foreach(pollingThread => pollingThread.recordConsumer = Some(consumer))
  }

  private def initConsumers(): Unit = {
    this.consumersGroup = Some(new Array[KafkaConsumer[String, String]](Vaccine.values.size))
    this.consumersGroupThreads = Some(new Array[PollingThread[String, String]](Vaccine.values.size))

    for (i <- this.consumersGroup.get.indices) {
      this.consumersGroup.get(i) = new KafkaConsumer[String, String](properties)
      this.consumersGroup.get(i).assign(List(new TopicPartition(LUBM1stream.outputTopicName, i)).asJava)
      this.consumersGroupThreads.get(i) = new PollingThread[String, String](this.consumersGroup.get(i))
    }
  }

  def init(): Unit = {
    this.admin = Some(AdminClient.create(properties))

    initTopics()
    initProducers()
    initConsumers()

    // Building topology
    List(
      new LUBM1stream(),
      new SideEffectStream()
    ).foreach(stream => stream.build())
  }

  def start(): Unit = {
    this.kafkaStreams = Some(new KafkaStreams(fr.uge.sair.kafka.streams.Stream.builder.build(), properties))
    this.kafkaStreams.get.start()
  }

  def startPolling(): Unit = {
    this.consumersGroupThreads.get.foreach(pollingThread => pollingThread.start())
  }

  def stopPolling(): Unit = {
    this.consumersGroupThreads.get.foreach(pollingThread => pollingThread.interrupt())
  }

  def close(): Unit = {
    this.admin.get.close()
    this._producer.get.close()
    this.consumersGroup.get.foreach(consumer => consumer.close())
    this.kafkaStreams.get.close()
  }
}
