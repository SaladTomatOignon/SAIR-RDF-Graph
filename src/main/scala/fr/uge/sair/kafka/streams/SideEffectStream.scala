package fr.uge.sair.kafka.streams

import fr.uge.sair.lumb1.SideEffectRecord
import fr.uge.sair.serialization.JacksonSerializer
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

  override val inputTopicName: String = SideEffectStream.inputTopicName
  override val outputTopicName: String = SideEffectStream.outputTopicName

  override def build(): Unit = {
    // Source input
    val sourceProcessor = getBuilder.stream[String, String](inputTopicName, Consumed.`with`(stringSerde, stringSerde))

    // Anonymizing the stream (= removing firstName and lastName from record)
    val anonymizedNode = sourceProcessor.mapValues[String](
      new ValueMapper[String, String]() {
        override def apply(value: String): String = {
          val mapper = JacksonSerializer.mapper
          val sideEffectRecord = mapper.readValue(value, classOf[SideEffectRecord])

          mapper.writeValueAsString(SideEffectRecord(
            sideEffectRecord.id, null, null, sideEffectRecord.vaccinationDate, sideEffectRecord.vaccine, sideEffectRecord.siderCode))
        }
      }
    )

    // Writing the result to a new topic
    anonymizedNode.to(outputTopicName, Produced.`with`(stringSerde, stringSerde))
  }
}

object SideEffectStream {
  val inputTopicName: String = "side-effect"
  val outputTopicName: String = "anonymous-side-effect"
}