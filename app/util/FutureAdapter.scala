package util

import com.datastax.driver.core.{ResultSet, ResultSetFuture}
import com.google.common.util.concurrent.{FutureCallback, Futures}

import scala.concurrent.{Future, Promise}

trait FutureAdapter {
  /** Adapts Cassandra async result to Scala future
    *
    * @param r ResultSetFuture
    * @return Future[ResultSet]
    */
  implicit def futureToScala(r: ResultSetFuture): Future[ResultSet] = {
    val p = Promise[ResultSet]()
    Futures.addCallback(r,
      new FutureCallback[ResultSet] {
        def onSuccess(r: ResultSet): Unit = p.success(r)
        def onFailure(t: Throwable): Unit = p.failure(t)
      })
    p.future
  }
}
