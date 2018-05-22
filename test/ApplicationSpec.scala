import java.util.Date

import clients.CassandraClient
import controllers.EventController
import dao._
import models._
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play._
import play.api.libs.json.{Writes, Json}
import play.api.test.Helpers._
import play.api.test._

import scala.collection.mutable
import scala.util.Random

class ApplicationSpec extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {

  val testSrcId = "src1"
  val testEvents = mutable.ArrayBuffer.empty[Event]

  def createTestEvent(srcId: String): Event = {
    Thread.sleep(Random.nextInt(100))

    val evt = Event(
      srcId = srcId,
      ts = new Date(),
      count = Random.nextInt(10)
    )

    testEvents.append(evt)
    evt
  }

  val client = new CassandraClient()
  val table = new EventTable(client)
  val eventController = new EventController(table)

  "EventController" should {

    "store events" in {

      val results = 1 to 10 map{ i =>
        val evt = createTestEvent(testSrcId)

        println(s"Creating Event $i: ${pprint(evt)}")

        val result = eventController.save()(FakeRequest().withBody(evt))
        status(result)
      }

      results.size mustBe 10
      results.forall(_ == CREATED) mustBe true
    }

    "get latest event by id" in {

      val result = eventController.getLatest(testSrcId)(FakeRequest())
      val eventResult = contentAsJson(result).as[Event]

      println(s"\nLatest Event:${pprint(eventResult)}")

      val latestEvent = testEvents.maxBy(_.ts)

      status(result) mustBe OK
      eventResult mustEqual latestEvent
    }

    "get a range of events" in {

      val slice = testEvents.slice(2, 8)

      val fromDate = Some(slice.head.ts.getTime)
      val toDate = Some(slice.last.ts.getTime)

      val result = eventController.getRange(testSrcId, fromDate, toDate)(FakeRequest())
      val eventResults = contentAsJson(result).as[Seq[Event]]

      println(s"\nRange of Events between ${fromDate.get} and ${toDate.get}: ${pprint(eventResults)}")

      status(result) mustBe OK
      eventResults mustEqual slice.reverse
    }

    "get a summary of events" in {

      val slice = testEvents.slice(2, 8)

      val fromDate = Some(slice.head.ts.getTime)
      val toDate = Some(slice.last.ts.getTime)

      val result = eventController.getSummary(testSrcId, fromDate, toDate)(FakeRequest())
      val summaryResult = contentAsJson(result).as[EventSummary]

      println(s"\nEvent Summary: ${pprint(summaryResult)}")

      val summary = EventSummary(
        from = slice.head.ts,
        to = slice.last.ts,
        count = slice.size,
        sum = slice.map(_.count).sum,
        avg = slice.map(_.count).sum.toDouble / slice.size
      )

      status(result) mustBe OK
      summaryResult mustEqual summary
    }

  }

  override def afterAll() = client.close()

  def pprint[A](a: A)(implicit w: Writes[A]) = s"\n${Json.prettyPrint(Json.toJson(a))}\n"

}