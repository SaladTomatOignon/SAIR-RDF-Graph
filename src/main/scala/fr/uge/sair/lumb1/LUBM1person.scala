package fr.uge.sair.lumb1

import fr.uge.sair.faker.FakeData

class LUBM1person(val id: String, val firstName: String, val lastName: String, val gender: String, val zipcode: String,
                  val state: String, val birthday: String, val vaccinationDate: String, val vaccine: String) {

  val isVaccinated: Boolean = !vaccinationDate.isBlank

  def getRecordWithFakeSideEffect: LUBM1record = {
    LUBM1record(id, firstName, lastName, vaccinationDate, vaccine, LUBM1person.fakeData.siderEffectCode())
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

    new LUBM1person(fakeData.id().toString,
      fakeData.firstName(),
      fakeData.lastName(),
      fakeData.gender(maleProportion),
      fakeData.zipcode(),
      fakeData.state(),
      fakeData.birthday(minAge, maxAge),
      if (isVaccinated) fakeData.vaccinationDate() else "",
      if (isVaccinated) fakeData.vaccine() else "")
  }
}