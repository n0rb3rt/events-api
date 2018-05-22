Summary
-------

This example Play Framework application demonstrates a microservice that persists and retrieves "events" (described in JSON) to/from
a Cassandra table.  It uses an embedded instance of Cassandra to illustrate the storage layer.

Instructions
------------

Implement the following methods to complete the application, and pass the tests:

+ app/dao/EventTable.scala
  + save -- Save an event object to Cassandra
  + getLatest -- Get the most recent Event for a given id from Cassandra
  + getRange -- Get a range of Events for a given id from Cassandra
  + getSummary -- Summarize a range of Events for a given id
  + createSchema -- Create the Cassandra column family schema
+ app/controllers/EventController.scala
  + save -- API endpoint to save Event JSON
  + getLatest -- API endpoint to return JSON for latest Event
  + getRange -- API endpoint to return JSON for range of Events
  + getSummary -- API endpoint to return JSON for EventSummary
  
If time is short, focus on completing one or more endpoints end-to-end.

References:

+ [Cassandra schema definition](https://docs.datastax.com/en/cql/3.1/cql/cql_reference/create_table_r.html?scroll=reference_ds_v3f_vfk_xj__ordering-results)
+ [Querying Cassandra with Java/Scala](https://docs.datastax.com/en/drivers/java/2.0/com/datastax/driver/core/querybuilder/QueryBuilder.html)
+ [Play Framework controllers](https://www.playframework.com/documentation/2.5.x/ScalaActions)
+ [Installing sbt on Mac](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Mac.html)

To run tests:
`sbt test`

Expected test results:
```
[info] ScalaTest
[info] Run completed in 13 seconds, 541 milliseconds.
[info] Total number of tests run: 4
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 4, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[info] Passed: Total 4, Failed 0, Errors 0, Passed 4
[success] Total time: 14 s, completed Jun 11, 2016 11:18:34 AM
```

APIs
----

##### Create event
###### Request
`POST /events`
```json
{
  "srcId": "src1",
  "ts": 1465669723800,
  "count": 3
}
```
###### Response
```
201 Created
```
```
Saved event ID: src1
```

##### Get latest event by srcId
###### Request
`GET /events/:id`
###### Response
`200 OK`
```json
{
  "srcId": "src1",
  "ts": 1465669723800,
  "count": 3
}
```

##### Get a range of events
###### Request
`GET /events/:id/range[?from=1465669723800&to=1465669907657]`
###### Response
`200 OK`
```json
[
  {
    "srcId": "src1",
    "ts": 1465669907657,
    "count": 3
  },
  {
    "srcId": "src1",
    "ts": 1465669723800,
    "count": 3
  }
]
```

##### Get a summary of events
###### Request
`GET /events/:id/summary[?from=1465669723800&to=1465669907657]`
###### Response
`200 OK`
```json
{
  "from": 1465669723800,
  "to": 1465669907657,
  "count": 2,
  "sum": 6,
  "avg": 3
}
```
