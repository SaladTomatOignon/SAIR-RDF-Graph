package fr.uge.sair.main

import fr.uge.sair.graphs.LUBM1graph
import fr.uge.sair.kafka.Kafka
import fr.uge.sair.serialization.JacksonSerializer
import fr.uge.sair.kafka.streams.{LUBM1stream, SideEffectStream}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.log4j.BasicConfigurator

import java.time.Duration
import java.util.concurrent.TimeUnit

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
    Kafka.producer.get.send(new ProducerRecord[String, String](SideEffectStream.inputTopicName,
      person.vaccine,
      JacksonSerializer.mapper.writeValueAsString(person.generateRecordWithFakeSideEffect)))
  })

  // Supplying data to the second topic with the side effects records (dynamic data)
  graph.getVaccinatedLUBM1Persons.foreach(person => {
    Kafka.producer.get.send(new ProducerRecord[String, String](LUBM1stream.inputTopicName,
      person.vaccine,
      JacksonSerializer.mapper.writeValueAsString(person)))
  })

  TimeUnit.MILLISECONDS.sleep(3000)

  Kafka.consumersGroup.get.foreach(consumer => {
    consumer.poll(Duration.ofMillis(1000)).forEach(record => {
      println(record.partition() + " " + record.value())
    })
  })

  Kafka.close()
}
