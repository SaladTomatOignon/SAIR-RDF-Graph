package fr.uge.sair.lumb1

import fr.uge.sair.faker.FakeData

class LUBM1person(id: String, firstName: String, lastName: String, gender: String, zipcode: String,
                       state: String, birthday: String, vaccinationDate: String, vaccine: String) {

  val isVaccinated: Boolean = !vaccinationDate.isBlank
}

object LUBM1person {
  val fakeData: FakeData = FakeData()
  val maleProportion = 48
  val vaccinesProportion = 10

  val studentTypes = List("GraduateStudent", "UndergraduateStudent")
  val professorTypes = List("TeachingAssistant", "AssociateProfessor", "ResearchAssistant", "FullProfessor", "Lecturer", "AssistantProfessor")

  def isStudent(personType: String) : Boolean = studentTypes.contains(personType)
  def isProfessor(personType: String) : Boolean = professorTypes.contains(personType)

  def apply(personType: String) : LUBM1person = {
    val minAge = if (isStudent(personType)) 20 else if (isProfessor(personType)) 30 else throw new IllegalArgumentException("Invalid person type")
    val maxAge = if (isStudent(personType)) 30 else if (isProfessor(personType)) 70 else throw new IllegalArgumentException("Invalid person type")
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