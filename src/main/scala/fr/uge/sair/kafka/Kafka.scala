package fr.uge.sair.kafka

import fr.uge.sair.kafka.streams.{LUBM1stream, SideEffectStream}
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import java.util.Properties
import java.util.concurrent.{ExecutionException, TimeUnit}
import scala.collection.JavaConverters.{asScalaSet, seqAsJavaListConverter}

object Kafka {
  val appName: String = "LUBM-Vaccinations"
  private var kafkaStreams: Option[KafkaStreams] = None
  private var admin: Option[AdminClient] = None
  var consumersGroup: Option[Array[KafkaConsumer[String, String]]] = None
  var producer: Option[KafkaProducer[String, String]] = None

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

  private def initTopics(): Unit = {
    val topics = List(
      new NewTopic(LUBM1stream.inputTopicName, 5, 1.toShort),
      new NewTopic(LUBM1stream.outputTopicName, 5, 1.toShort),
      new NewTopic(SideEffectStream.inputTopicName, 5, 1.toShort),
      new NewTopic(SideEffectStream.outputTopicName, 5, 1.toShort)
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

          // Sleep time because this deleteTopics() method does not perform his action despite the get() method call
          TimeUnit.MILLISECONDS.sleep(1000)
          this.admin.get.createTopics(topics.asJava).all().get()
      }
    }
  }

  private def initProducers(): Unit = {
    this.producer = Some(new KafkaProducer[String, String](properties))
  }

  private def initConsumers(): Unit = {
    this.consumersGroup = Some(new Array[KafkaConsumer[String, String]](5))

    for (i <- this.consumersGroup.get.indices) {
      this.consumersGroup.get(i) = new KafkaConsumer[String, String](properties)
      this.consumersGroup.get(i).assign(List(new TopicPartition(LUBM1stream.outputTopicName, i)).asJava)
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

  def close(): Unit = {
    this.admin.get.close()
    this.producer.get.close()
    this.consumersGroup.get.foreach(consumer => consumer.close())
    this.kafkaStreams.get.close()
  }
}
