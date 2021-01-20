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

      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + "id"), model.createLiteral(fakePerson.id))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + "fName"), model.createLiteral(fakePerson.firstName))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + "lName"), model.createLiteral(fakePerson.lastName))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + "gender"), model.createResource(LUBM1graph.extensionPropertyNamespace + "#" + fakePerson.gender))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + "zipcode"), model.createLiteral(fakePerson.zipcode))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + "state"), model.createLiteral(fakePerson.state))
      model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + "birthday"), model.createLiteral(fakePerson.birthday))

      if (fakePerson.isVaccinated) {
        model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + "vaccinationDate"), model.createLiteral(fakePerson.vaccinationDate))
        model.add(person, model.createProperty(LUBM1graph.extensionPropertyNamespace + "#" + "vaccine"), model.createResource(LUBM1graph.extensionPropertyNamespace + "#" + fakePerson.vaccine))
      }
    })
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
}