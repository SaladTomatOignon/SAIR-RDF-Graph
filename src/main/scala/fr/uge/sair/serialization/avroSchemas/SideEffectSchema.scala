package fr.uge.sair.serialization.avroSchemas

class SideEffectSchema(schemaPath: String) extends AvroSchema(schemaPath) {

}

object SideEffectSchema {
  val schemaPath: String = getClass.getResource("/SideEffectRecord.avsc").getPath

  def apply(): SideEffectSchema = new SideEffectSchema(schemaPath)
}
