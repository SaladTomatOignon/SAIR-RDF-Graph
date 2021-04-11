package fr.uge.sair.main

import fr.uge.sair.graphs.LUBM1graph
import fr.uge.sair.kafka.Kafka
import fr.uge.sair.kafka.streams.{LUBM1stream, SideEffectStream}
import fr.uge.sair.lumb1.LUBM1person
import fr.uge.sair.serialization.avroSchemas.{Lubm1personSchema, SideEffectSchema}
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
    Kafka.producer.send(
      new ProducerRecord[String, Array[Byte]](
        LUBM1stream.inputTopicName,
        person.vaccine,
        Lubm1personSchema().recordInjection.apply(person.toRecord)
      )
    )
  })

  // Supplying data to the second topic with the side effects records (dynamic data)
  var compteurSent = 0
  graph.getVaccinatedLUBM1Persons.foreach(person => {
    compteurSent = compteurSent + 1
    Kafka.producer.send(
      new ProducerRecord[String, Array[Byte]](
        SideEffectStream.inputTopicName,
        person.vaccine,
        SideEffectSchema().recordInjection.apply(person.generateRecordWithFakeSideEffect.toRecord)
      )
    )
  })

  var compteurReceived = 0
  Kafka.setConsumingFunction(record => {
    compteurReceived = compteurReceived + 1
    val person = LUBM1person.fromRecord(Lubm1personSchema().recordInjection.invert(record.value()).get)
    println(record.key() + " " + person)
  })

  Kafka.startPolling()
  scala.io.StdIn.readLine()
  Kafka.stopPolling()

  Kafka.close()

  println("Envoyé : " + compteurSent)
  println("Recu : " + compteurReceived)

  //graph.export("output.ttl")
}
