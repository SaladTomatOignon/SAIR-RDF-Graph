package fr.uge.sair

import fr.uge.sair.graphs.LUBM1graph
import fr.uge.sair.lumb1.LUBM1person
import org.scalatest.FunSuite

class DataProportionsTest extends FunSuite {
  val EPSILON = 0.1 // Margin of error

  test("Male proportion") {
    assert(almostEqual(maleProportion(), LUBM1person.maleProportion / 100.0))
  }

  test("Vaccinated people proportion") {
    assert(almostEqual(vaccinatedProportion(), LUBM1person.vaccinesProportion / 100.0))
  }

  def maleProportion(): Double = {

    @scala.annotation.tailrec
    def aux(n: Int, acc: Double): Double = {
      if (n == 0) acc
      else {
        val graph = LUBM1graph()
        graph.load()
        graph.extendPersonsWithFakeData()

        val persons = graph.listPersons()
        val nbMale = persons.count(person => graph.getPersonGender(person).getLocalName.equals("Male"))
        aux(n-1, acc + (nbMale.toDouble / persons.length))
      }
    }

    val n = 10
    aux(n, 0) / n
  }

  def vaccinatedProportion(): Double = {

    @scala.annotation.tailrec
    def aux(n: Int, acc: Double): Double = {
      if (n == 0) acc
      else {
        val graph = LUBM1graph()
        graph.load()
        graph.extendPersonsWithFakeData()

        val persons = graph.listPersons()
        val nbVaccinated = persons.count(graph.isPersonVaccinated)
        aux(n-1, acc + (nbVaccinated.toDouble / persons.length))
      }
    }

    val n = 10
    aux(n, 0) / n
  }

  def almostEqual(actual: Double, expected: Double): Boolean = expected - EPSILON <= actual && actual <= expected + EPSILON

}
