package fr.uge.sair.main
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fr.uge.sair.graphs.LUBM1graph

object Main extends App {
  val graph = LUBM1graph()
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  graph.load()
  println("Number of statements with original graph : " + graph.size())

  graph.extendPersonsWithFakeData()
  println("Number of statements with extended graph : " + graph.size())

  graph.getVaccinatedLUBM1Persons.foreach(person => {
    println(mapper.writeValueAsString(person.getRecordWithFakeSideEffect))
  })

  graph.export("output.ttl")
}
