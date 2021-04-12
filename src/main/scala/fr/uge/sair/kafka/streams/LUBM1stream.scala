package fr.uge.sair.kafka.streams

import fr.uge.sair.lumb1.{LUBM1person, SideEffectRecord}
import fr.uge.sair.serialization.JacksonSerializer
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{Consumed, JoinWindows, Produced, StreamJoined, ValueJoiner}

import java.time.Duration

/**
 * Corresponds to the "static data" from the project
 *
 * This stream aims at adding side effect on vaccinated people, from another stream.
 * Takes as input a stream of {@link LUBM1person} WITHOUT sideEffectCode and gives out a stream of {@link LUBM1person}
 * WITH sideEffectCode by joining this stream with the {@link SideEffectStream}
 */
class LUBM1stream extends Stream {
  val stringSerde: Serde[String] = Serdes.String()

  override val inputTopicName: String = LUBM1stream.inputTopicName
  override val outputTopicName: String = LUBM1stream.outputTopicName

  override def build(): Unit = {
    // Source input
    val sourceProcessor = getBuilder.stream[String, String](inputTopicName, Consumed.`with`(stringSerde, stringSerde))
    val sideEffectStream = getBuilder.stream[String, String](SideEffectStream.outputTopicName, Consumed.`with`(stringSerde, stringSerde))

    // Joining node : it joins records of this stream and of the SideEffectStream with same IDs
    val joinNode = sourceProcessor.join[String, String](sideEffectStream, new ValueJoiner[String, String, String]() {
      override def apply(value1: String, value2: String): String = {
        val mapper = JacksonSerializer.mapper
        val lubm1person = mapper.readValue(value1, classOf[LUBM1person])
        val sideEffectRecord = mapper.readValue(value2, classOf[SideEffectRecord])

        if (lubm1person.id.equals(sideEffectRecord.id)) {
          mapper.writeValueAsString(lubm1person.copy(sideEffectCode = sideEffectRecord.siderCode))
        } else {
          ""
        }
      }
    }, JoinWindows.of(Duration.ofMillis(LUBM1stream.joinWindowInterval)), StreamJoined.`with`[String, String, String](stringSerde, stringSerde, stringSerde))

    // Filtering empty records, which represents here the values which could not be joined (because of different IDs)
    val filteredStream = joinNode.filter((k: String, v:String) => {
      v.nonEmpty
    })

    // Writing the result to a new topic
    filteredStream.to(outputTopicName, Produced.`with`[String, String](stringSerde, stringSerde))
  }
}

object LUBM1stream {
  val inputTopicName: String = "LUBM1-vaccinated-persons"
  val outputTopicName: String = "LUBM1-vaccinated-persons-side-effect"

  /**
  * The interval time (in milliseconds) in which 2 values can be joined
   * in the joining node
  */
  val joinWindowInterval: Long = 1000
}