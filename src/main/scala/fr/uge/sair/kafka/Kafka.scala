package fr.uge.sair.kafka

import fr.uge.sair.kafka.streams.{LUBM1stream, SideEffectStream}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import java.util.Properties
import scala.collection.JavaConverters.seqAsJavaListConverter

object Kafka {
  private var kafkaStreams: Option[KafkaStreams] = None
  var consumersGroup: Option[Array[KafkaConsumer[String, String]]] = None
  var producer: Option[KafkaProducer[String, String]] = None

  private def properties: Properties = {
    val props = new Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "LUBM-Vaccinations")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "Vaccines-consumers")

    props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "fr.uge.sair.kafka.VaccinePartitioner")

    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")

    props
  }

  private def initProducers(): Unit = {
    producer = Some(new KafkaProducer[String, String](properties))
  }

  private def initConsumers(): Unit = {
    consumersGroup = Some(new Array[KafkaConsumer[String, String]](5))
    for (i <- consumersGroup.get.indices) {
      consumersGroup.get(i) = new KafkaConsumer[String, String](properties)
      consumersGroup.get(i).assign(List(new TopicPartition(LUBM1stream.outputTopicName, i)).asJava)
    }
  }

  def init(): Unit = {
    initProducers()
    initConsumers()

    // Building topology
    List(new LUBM1stream(), new SideEffectStream()).foreach(stream => stream.build())
  }

  def start(): Unit = {
    kafkaStreams = Some(new KafkaStreams(fr.uge.sair.kafka.streams.Stream.builder.build(), properties))
    kafkaStreams.get.start()
  }

  def close(): Unit = {
    producer.get.close()
    consumersGroup.get.foreach(consumer => consumer.close())
    kafkaStreams.get.close()
  }
}
