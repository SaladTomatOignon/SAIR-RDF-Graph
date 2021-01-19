package fr.uge.sair.graphs

import org.apache.jena.rdf.model.{Model, ModelFactory}

import scala.collection.mutable.ListBuffer

class RDFgraph(val source: String, val language: String) {
  val model = ModelFactory.createDefaultModel()

  def load() : Model =  model.read(source, language)

  def size() : Long = model.size()

  def listProperties() : ListBuffer[String] = {
    val stmtIt = model.listStatements()
    var properties = new ListBuffer[String]()

    while(stmtIt.hasNext()) {
      properties += stmtIt.next.getPredicate.getURI
    }

    properties
  }
}
