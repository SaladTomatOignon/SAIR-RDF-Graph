package fr.uge.sair.data

object Vaccine extends Enumeration {
  type Vaccine = Value

  val PFIZER = Value("Pfizer")
  val MODERNA = Value("Moderna")
  val ASTRAZENECA = Value("AstraZeneca")
  val SPOUTNIKV = Value("SpoutnikV")
  val CANSINOBIO = Value("CanSinoBio")
}
