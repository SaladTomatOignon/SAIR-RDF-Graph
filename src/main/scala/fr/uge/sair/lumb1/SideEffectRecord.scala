package fr.uge.sair.lumb1

import fr.uge.sair.serialization.avroSchemas.SideEffectSchema
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}
import org.apache.avro.util.Utf8

import java.nio.charset.StandardCharsets

case class SideEffectRecord(val id: Int, val firstName: String, val lastName: String,
                            val vaccinationDate: String, val vaccine: String, val siderCode: String) {

  def toRecord: GenericRecord = {
    new GenericRecordBuilder(SideEffectSchema().schema)
      .set("id", id)
      .set("firstName", firstName)
      .set("lastName", lastName)
      .set("vaccinationDate", vaccinationDate)
      .set("vaccine", vaccine)
      .set("siderCode", siderCode)
    .build()
  }
}

object SideEffectRecord {
  def fromRecord(record: GenericRecord): SideEffectRecord = {
    val firstName = record.get("firstName")
    val lastName = record.get("lastName")

    SideEffectRecord(
      record.get("id").asInstanceOf[Int],
      if (firstName != null) firstName.toString else "",
      if (lastName != null) lastName.toString else "",
      record.get("vaccinationDate").toString,
      record.get("vaccine").toString,
      record.get("siderCode").toString
    )
  }
}