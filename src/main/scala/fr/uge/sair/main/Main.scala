package fr.uge.sair.main

import fr.uge.sair.graphs.LUBM1graph

object Main extends App {
  val graph = LUBM1graph()

  graph.load()
  println("Number of statements with original graph : " + graph.size())

  graph.extendPersonsWithFakeData()
  println("Number of statements with extended graph : " + graph.size())
}
