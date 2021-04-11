package fr.uge.sair.serialization.avroSchemas

import com.twitter.bijection.Injection
import com.twitter.bijection.avro.GenericAvroCodecs
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord

import java.io.FileInputStream

abstract class AvroSchema(schemaPath: String) {
  private val inputStream = new FileInputStream(schemaPath)
  private val parser = new Schema.Parser()
  val schema: Schema = parser.parse(inputStream)
  val recordInjection: Injection[GenericRecord, Array[Byte]] = GenericAvroCodecs.toBinary(schema)
}
