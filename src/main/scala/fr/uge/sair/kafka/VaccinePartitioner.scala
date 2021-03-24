package fr.uge.sair.kafka

import fr.uge.sair.data.Vaccine
import org.apache.kafka.clients.producer.Partitioner
import org.apache.kafka.common.Cluster

import java.util

class VaccinePartitioner extends Partitioner {
  override def partition(topic: String, key: Any, keyBytes: Array[Byte], value: Any, valueBytes: Array[Byte], cluster: Cluster): Int = {
    key match {
      case str: String =>
        Vaccine.withName(str).id
      case bytes: Array[Byte] =>
        Vaccine.withName(new String(bytes)).id
    }
  }

  override def close(): Unit = {}

  override def configure(configs: util.Map[String, _]): Unit = {}
}
