package fr.uge.sair.faker

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, GregorianCalendar, Locale}

import com.github.javafaker.Faker

class FakeData(locale: Locale) {
  val faker = new Faker(locale)
  private var _id = 0

  def id() : Int = {
    _id += 1
    _id - 1
  }

  def firstName() : String = faker.name().firstName()

  def lastName() : String = faker.name().lastName()

  def gender(maleProportion: Int) : String = {
    if (faker.random.nextInt(101) < maleProportion) "Male" else "Female"
  }

  def zipcode() : String = faker.address.zipCode

  def state() : String = faker.address.state

  def birthday(minAge: Int, maxAge: Int) : String = FakeData.dateFormatter.format(faker.date.birthday(minAge, maxAge))

  def vaccinationDate() : String = {
    val today = Calendar.getInstance
    today.set(Calendar.HOUR_OF_DAY, 0)

    // Random date from FakeData.vaccinationStartDate
    FakeData.dateFormatter.format(faker.date.between(FakeData.vaccinationStartDate, today.getTime))
  }

  def vaccine() : String = {
    val vaccinesRepartitionCumul = FakeData.vaccinesRepartition
                                   .sortBy(_._2)
                                   .map{var cumul = 0.0; node => {cumul += node._2; (node._1, cumul)}}

    val random = faker.random().nextDouble()
    vaccinesRepartitionCumul.find(v => random <= v._2).get._1
  }
}

object FakeData {
  val dateFormatter = new SimpleDateFormat("dd/MM/yyyy")
  val vaccinationStartDate: Date = new GregorianCalendar(2020, 12, 1).getTime

  val vaccinesRepartition = List(("Pfizer", 0.2), ("Moderna", 0.12), ("AstraZeneca", 0.45),
                                 ("SpoutnikV", 0.15), ("CanSinoBio", 0.08))

  def apply(): FakeData = new FakeData(Locale.FRENCH)
}