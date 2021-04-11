package fr.uge.sair.kafka.streams

import fr.uge.sair.lumb1.SideEffectRecord
import fr.uge.sair.serialization.JacksonSerializer
import fr.uge.sair.serialization.avroSchemas.SideEffectSchema
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{Consumed, Produced, ValueMapper}

/**
 * Corresponds to the "dynamic data" from the project
 *
 * This stream aims at anonymizing the input data.
 * Takes as input a stream of {@link SideEffectRecord} and gives out a stream of {@link SideEffectRecord}
 * without the firstname and the lastname
 */
class SideEffectStream extends Stream {
  val stringSerde: Serde[String] = Serdes.String()
  val bytesSerde: Serde[Array[Byte]] = Serdes.ByteArray()

  override val inputTopicName: String = SideEffectStream.inputTopicName
  override val outputTopicName: String = SideEffectStream.outputTopicName

  override def build(): Unit = {
    // Source input
    val sourceProcessor = getBuilder.stream[String, Array[Byte]](inputTopicName, Consumed.`with`(stringSerde, bytesSerde))

    // Anonymizing the stream (= removing firstName and lastName from record)
    val anonymizedNode = sourceProcessor.mapValues[Array[Byte]](
      new ValueMapper[Array[Byte], Array[Byte]]() {
        override def apply(value: Array[Byte]): Array[Byte] = {
          val sideEffectRecord = SideEffectRecord.fromRecord(SideEffectSchema().recordInjection.invert(value).get)

          SideEffectSchema().recordInjection.apply(SideEffectRecord(
            sideEffectRecord.id, null, null, sideEffectRecord.vaccinationDate, sideEffectRecord.vaccine, sideEffectRecord.siderCode).toRecord)
        }
      }
    )

    // Writing the result to a new topic
    anonymizedNode.to(outputTopicName, Produced.`with`(stringSerde, bytesSerde))
  }
}

object SideEffectStream {
  val inputTopicName: String = "side-effect"
  val outputTopicName: String = "anonymous-side-effect"
}