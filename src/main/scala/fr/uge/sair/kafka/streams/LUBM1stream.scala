package fr.uge.sair.kafka.streams

import fr.uge.sair.lumb1.{LUBM1person, SideEffectRecord}
import fr.uge.sair.serialization.JacksonSerializer
import fr.uge.sair.serialization.avroSchemas.{Lubm1personSchema, SideEffectSchema}
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
  val bytesSerde: Serde[Array[Byte]] = Serdes.ByteArray()

  override val inputTopicName: String = LUBM1stream.inputTopicName
  override val outputTopicName: String = LUBM1stream.outputTopicName

  override def build(): Unit = {
    // Source input
    val sourceProcessor = getBuilder.stream[String, Array[Byte]](inputTopicName, Consumed.`with`(stringSerde, bytesSerde))
    val sideEffectStream = getBuilder.stream[String, Array[Byte]](SideEffectStream.outputTopicName, Consumed.`with`(stringSerde, bytesSerde))

    // Joining node : it joins records of this stream and of the SideEffectStream with same IDs
    val joinNode = sourceProcessor.join[Array[Byte], Array[Byte]](
      sideEffectStream,
      valueJoiner,
      JoinWindows.of(Duration.ofMillis(LUBM1stream.joinWindowInterval)),
      StreamJoined.`with`[String, Array[Byte], Array[Byte]](stringSerde, bytesSerde, bytesSerde)
    )

    // Filtering empty records, which represents here the values which could not be joined (because of different IDs)
    val filteredStream = joinNode.filter((k: String, v:Array[Byte]) => {
      v.nonEmpty
    })

    // Writing the result to a new topic
    filteredStream.to(outputTopicName, Produced.`with`[String, Array[Byte]](stringSerde, bytesSerde))
  }

  val valueJoiner: ValueJoiner[Array[Byte], Array[Byte], Array[Byte]] = {
    new ValueJoiner[Array[Byte], Array[Byte], Array[Byte]]() {
      override def apply(value1: Array[Byte], value2: Array[Byte]): Array[Byte] = {
        val lubm1person = LUBM1person.fromRecord(Lubm1personSchema().recordInjection.invert(value1).get)
        val sideEffectRecord = SideEffectRecord.fromRecord(SideEffectSchema().recordInjection.invert(value2).get)

        if (lubm1person.id.equals(sideEffectRecord.id)) {
          Lubm1personSchema().recordInjection.apply(
            lubm1person.copy(sideEffectCode = sideEffectRecord.siderCode).toRecord
          )
        } else {
          new Array[Byte](0)
        }
      }
    }
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