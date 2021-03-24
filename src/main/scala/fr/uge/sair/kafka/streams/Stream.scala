package fr.uge.sair.kafka.streams

import org.apache.kafka.streams.StreamsBuilder

abstract class Stream {
  def getBuilder: StreamsBuilder = Stream.builder
  def build(): Unit

  def inputTopicName(): String
  def outputTopicName(): String
}

object Stream {
  val builder = new StreamsBuilder()
}