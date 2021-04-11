package fr.uge.sair.serialization.avroSchemas

class Lubm1personSchema(schemaPath: String) extends AvroSchema(schemaPath) {

}

object Lubm1personSchema {
  val schemaPath: String = getClass.getResource("/Lubm1record.avsc").getPath

  def apply(): Lubm1personSchema = new Lubm1personSchema(schemaPath)
}
