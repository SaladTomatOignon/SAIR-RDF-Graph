package fr.uge.sair.lumb1

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.uge.sair.faker.FakeData
import fr.uge.sair.serialization.avroSchemas.Lubm1personSchema
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}

import java.nio.charset.StandardCharsets

case class LUBM1person(id: Int, firstName: String, lastName: String, gender: String, zipcode: String, state: String,
                       birthday: String, vaccinationDate: String, vaccine: String, sideEffectCode: String = null) {

  @JsonIgnore
  val isVaccinated: Boolean = !vaccinationDate.isBlank

  def generateRecordWithFakeSideEffect: SideEffectRecord = {
    SideEffectRecord(id, firstName, lastName, vaccinationDate, vaccine, LUBM1person.fakeData.siderEffectCode())
  }

  def toRecord: GenericRecord = {
    val builder = new GenericRecordBuilder(Lubm1personSchema().schema)
      builder
      .set("id", id)
      .set("firstName", firstName)
      .set("lastName", lastName)
      .set("gender", gender)
      .set("zipcode", zipcode)
      .set("state", state)
      .set("birthday", birthday)
      .set("vaccinationDate", vaccinationDate)
      .set("vaccine", vaccine)
      .set("siderCode", sideEffectCode)

    builder.build()
  }
}

object LUBM1person {
  val fakeData: FakeData = FakeData()
  val maleProportion = 48
  val vaccinesProportion = 10

  // According to lubm1 data, the following types correspond to students
  val studentTypes = List("GraduateStudent", "UndergraduateStudent")
  // According to lubm1 data, the following types correspond to students with an additional status
  val advancedStudentTypes = List("TeachingAssistant", "ResearchAssistant")
  // According to lubm1 data, the following types correspond to professors
  val professorTypes = List("AssociateProfessor", "FullProfessor", "Lecturer", "AssistantProfessor")

  private def oneContains(lstA: List[String], lstB: List[String]) : Boolean = {
    for (a <- lstA) if (lstB.contains(a)) return true
    false
  }

  def isStudent(personTypes: List[String]) : Boolean = oneContains(personTypes, studentTypes)
  def isAdvancedStudent(personTypes: List[String]) : Boolean = oneContains(personTypes, advancedStudentTypes)
  def isProfessor(personTypes: List[String]) : Boolean = oneContains(personTypes, professorTypes)

  def apply(personTypes: List[String]) : LUBM1person = {
    val minAge = if (isAdvancedStudent(personTypes)) 20 else if (isStudent(personTypes)) 25 else if (isProfessor(personTypes)) 30 else throw new IllegalArgumentException("Invalid person type")
    val maxAge = if (isStudent(personTypes) || isAdvancedStudent(personTypes)) 30 else if (isProfessor(personTypes)) 70 else throw new IllegalArgumentException("Invalid person type")
    val isVaccinated = fakeData.faker.random.nextInt(101) < vaccinesProportion

    new LUBM1person(fakeData.id(),
      fakeData.firstName(),
      fakeData.lastName(),
      fakeData.gender(maleProportion),
      fakeData.zipcode(),
      fakeData.state(),
      fakeData.birthday(minAge, maxAge),
      if (isVaccinated) fakeData.vaccinationDate() else "",
      if (isVaccinated) fakeData.vaccine() else "")
  }

  def fromRecord(record: GenericRecord): LUBM1person = {
    val siderCode = record.get("siderCode")

    LUBM1person(
      record.get("id").asInstanceOf[Int],
      record.get("firstName").toString,
      record.get("lastName").toString,
      record.get("gender").toString,
      record.get("zipcode").toString,
      record.get("state").toString,
      record.get("birthday").toString,
      record.get("vaccinationDate").toString,
      record.get("vaccine").toString,
      if (siderCode != null) siderCode.toString else ""
    )
  }
}