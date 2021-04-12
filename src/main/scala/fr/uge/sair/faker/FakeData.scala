package fr.uge.sair.faker

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, GregorianCalendar, Locale}
import com.github.javafaker.Faker
import fr.uge.sair.data.Vaccine.{ASTRAZENECA, CANSINOBIO, MODERNA, PFIZER, SPOUTNIKV}

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
    vaccinesRepartitionCumul.find(v => random <= v._2).get._1.toString
  }

  def siderEffectCode(): String = {
    val randIndex = faker.random().nextInt(FakeData.siderEffects.length)

    FakeData.siderEffects(randIndex)._1
  }

  def siderEffectCode(): String = {
    val randIndex = faker.random().nextInt(FakeData.siderEffects.length)

    FakeData.siderEffects(randIndex)._1
  }
}

object FakeData {
  val dateFormatter = new SimpleDateFormat("dd/MM/yyyy")
  val vaccinationStartDate: Date = new GregorianCalendar(2020, 12, 1).getTime

  val vaccinesRepartition = List((PFIZER, 0.2), (MODERNA, 0.12), (ASTRAZENECA, 0.45),
                                 (SPOUTNIKV, 0.15), (CANSINOBIO, 0.08))

  val siderEffects = List(("C0151828", "Injection site pain"), ("C0015672", "fatigue"), ("C0018681", "headache"), ("C0231528", "Muscle pain"),
                          ("C0085593", "chills"), ("C0003862", "Joint pain"), ("C0015967", "fever"), ("C0151605", "Injection site swelling"),
                          ("C0852625", "Injection site redness"), ("C0027497", "Nausea"), ("C0231218", "Malaise"), ("C0497156", "Lymphadenopathy"),
                          ("C0863083", "Injection site tenderness"))

  val siderEffects = List(("C0151828", "Injection site pain"), ("C0015672", "fatigue"), ("C0018681", "headache"), ("C0231528", "Muscle pain"),
                          ("C0085593", "chills"), ("C0003862", "Joint pain"), ("C0015967", "fever"), ("C0151605", "Injection site swelling"),
                          ("C0852625", "Injection site redness"), ("C0027497", "Nausea"), ("C0231218", "Malaise"), ("C0497156", "Lymphadenopathy"),
                          ("C0863083", "Injection site tenderness"))

  def apply(): FakeData = new FakeData(Locale.FRENCH)
}