package fr.uge.sair.main

import fr.uge.sair.graphs.LUBM1graph
import fr.uge.sair.kafka.Kafka
import fr.uge.sair.serialization.JacksonSerializer
import fr.uge.sair.kafka.streams.{LUBM1stream, SideEffectStream}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.log4j.BasicConfigurator


object Main extends App {
  BasicConfigurator.configure()
  val graph = LUBM1graph()

  graph.load()
  println("Number of statements with original graph : " + graph.size())

  graph.extendPersonsWithFakeData()
  println("Number of statements with extended graph : " + graph.size())

  Kafka.init()
  Kafka.start()

  // Supplying data to the first topic with LUBM1 persons (static data)
  graph.getVaccinatedLUBM1Persons.foreach(person => {
    Kafka.producer.send(new ProducerRecord[String, String](SideEffectStream.inputTopicName,
      person.vaccine,
      JacksonSerializer.mapper.writeValueAsString(person.generateRecordWithFakeSideEffect)))
  })

  // Supplying data to the second topic with the side effects records (dynamic data)
  graph.getVaccinatedLUBM1Persons.foreach(person => {
    Kafka.producer.send(new ProducerRecord[String, String](LUBM1stream.inputTopicName,
      person.vaccine,
      JacksonSerializer.mapper.writeValueAsString(person)))
  })

  Kafka.setConsumingFunction(record => {
    println(record.key() + " " + record.value())
  })

  Kafka.startPolling()
  scala.io.StdIn.readLine()
  Kafka.stopPolling()

  Kafka.close()

}
