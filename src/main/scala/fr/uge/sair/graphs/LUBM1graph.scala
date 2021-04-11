package fr.uge.sair.graphs

import fr.uge.sair.lumb1.LUBM1person
import org.apache.jena.rdf.model.Resource

import scala.collection.mutable.ListBuffer

class LUBM1graph(source: String, language: String) extends RDFgraph(source, language) {

  def listPersons(): List[Resource] = {
    val rdfTypeProperty = model.createProperty(LUBM1graph.typeProperty)
    var listPersons = ListBuffer[Resource]()

    LUBM1graph.occupations.foreach(occupation => {
      val rdfOccupation = model.createResource(LUBM1graph.typeObjectNamespace + "#" + occupation)
      model.listSubjectsWithProperty(rdfTypeProperty, rdfOccupation).forEach(person => listPersons += person)
    })

    listPersons.distinct.toList
  }

  def getPersonTypes(person: Resource) : List[Resource] = {
    val rdfTypeProperty = model.createProperty(LUBM1graph.typeProperty)
    var listPersonTypes = ListBuffer[Resource]()

    model.listObjectsOfProperty(person, rdfTypeProperty).forEach(occupation => listPersonTypes += occupation.asResource())

    listPersonTypes.toList
  }

  def extendPersonsWithFakeData(): Unit = {
    listPersons().foreach(person => {
      val fakePerson = LUBM1person(getPersonTypes(person).map(_.getLocalName))

      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.ID), model.createLiteral(fakePerson.id.toString))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.FIRST_NAME), model.createLiteral(fakePerson.firstName))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.LAST_NAME), model.createLiteral(fakePerson.lastName))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.GENDER), model.createResource(LUBM1graph.extensionPropertyNamespace + "#" + fakePerson.gender))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.ZIPCODE), model.createLiteral(fakePerson.zipcode))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.STATE), model.createLiteral(fakePerson.state))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.BIRTHDAY), model.createLiteral(fakePerson.birthday))

      if (fakePerson.isVaccinated) {
        model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.VACCINATION_DATE), model.createLiteral(fakePerson.vaccinationDate))
        model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.VACCINE), model.createResource(LUBM1graph.extensionPropertyNamespace + "#" + fakePerson.vaccine))
      }
    })
  }

  def getPersonGender(person: Resource): Resource = {
    val rdfGenderProperty = model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.GENDER)
    model.listObjectsOfProperty(person,rdfGenderProperty).nextNode().asResource()
  }

  def isPersonVaccinated(person: Resource): Boolean = {
    val rdfGenderProperty = model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.VACCINATION_DATE)
    model.listObjectsOfProperty(person, rdfGenderProperty).hasNext
  }

  def getVaccinatedPersons: List[Resource] = {
    val rdfVaccineProperty = model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.VACCINATION_DATE)
    var listPersons = ListBuffer[Resource]()

    model.listSubjectsWithProperty(rdfVaccineProperty).forEach(person => listPersons += person)

    listPersons.toList
  }

  def getVaccinatedLUBM1Persons: List[LUBM1person] = {
    var listPersons = ListBuffer[LUBM1person]()

    getVaccinatedPersons.foreach(person => {
      listPersons += new LUBM1person(
        model.listObjectsOfProperty(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.ID)).next.asLiteral.getInt,
        model.listObjectsOfProperty(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.FIRST_NAME)).next.asLiteral.getString,
        model.listObjectsOfProperty(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.LAST_NAME)).next.asLiteral.getString,
        model.listObjectsOfProperty(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.GENDER)).next.asResource.getLocalName,
        model.listObjectsOfProperty(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.ZIPCODE)).next.asLiteral.getString,
        model.listObjectsOfProperty(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.STATE)).next.asLiteral.getString,
        model.listObjectsOfProperty(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.BIRTHDAY)).next.asLiteral.getString,
        model.listObjectsOfProperty(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.VACCINATION_DATE)).next.asLiteral.getString,
        model.listObjectsOfProperty(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + LUBM1graph.Fragments.VACCINE)).next.asResource.getLocalName
      )
    })

    listPersons.toList
  }
}

object LUBM1graph {
  val source: String = getClass.getResource("/lubm1.ttl").getPath
  val language = "TTL"

  val occupations = List("TeachingAssistant", "GraduateStudent", "UndergraduateStudent", "AssociateProfessor",
                         "ResearchAssistant", "FullProfessor", "Lecturer", "AssistantProfessor")

  val typeProperty = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
  val typeObjectNamespace = "http://swat.cse.lehigh.edu/onto/univ-bench.owl"
  val extensionPropertyNamespace = "http://extension.group4.fr/onto"

  def apply(): LUBM1graph = new LUBM1graph("file:" + source, language)

  object Fragments {
    val ID = "id"
    val FIRST_NAME = "fname"
    val LAST_NAME = "lname"
    val GENDER = "gender"
    val ZIPCODE = "zipcode"
    val STATE = "state"
    val BIRTHDAY = "birthday"
    val VACCINATION_DATE = "vaccinationDate"
    val VACCINE = "vaccine"
  }
}