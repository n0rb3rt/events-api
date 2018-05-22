package models

import java.util.Date

import play.api.libs.json.{Json, Format}

case class Event(
  srcId: String,
  ts: Date,
  count: Int
)

object Event {
  //json (de)serializer for Event case class
  implicit val format: Format[Event] = Json.format[Event]
}

case class EventSummary(
  from: Date,
  to: Date,
  count: Int,
  sum: Int,
  avg: Double
)

object EventSummary {
  //json (de)serializer for EventSummary case class
  implicit val format: Format[EventSummary] = Json.format[EventSummary]
}