package org.mikhailgolubtsov.asyncpolling.executor

import org.mikhailgolubtsov.asyncpolling.executor.pollable.Pollable
import java.util.concurrent.{ScheduledExecutorService, TimeUnit}
import scala.concurrent.{ExecutionContext, Future, Promise}

class NonBlockingPollableExecutor[A: Pollable](es: ScheduledExecutorService) extends PollableExecutor[A] {

  implicit val ec = ExecutionContext.fromExecutor(es)

  override def execute(pollable: A): Future[Pollable.PollableStatus] = {
    val started = Pollable[A].start(pollable)
    println(s"executing statement: $started")

    val name = Pollable[A].name(pollable)

    val p = Promise[Pollable.PollableStatus]()

    val scheduledFuture = es.scheduleAtFixedRate(
      () => {
        println(s"polling $name")
        Pollable[A].status(started) match {
          case Pollable.PollableStatus.Pending => println(s"$name status pending"); p
          case Pollable.PollableStatus.Success =>
            println(s"$name succeed")
            p.success(Pollable.PollableStatus.Success)
          case f: Pollable.PollableStatus.Failure =>
            println(s"$name failed")
            p.success(f)

          case Pollable.PollableStatus.NotStarted =>
            p.success(
              Pollable.PollableStatus.Failure(new Exception("tried to get status of not started task"))
            )
          case _ => println("something really bad happened"); p.failure(new Exception("fatal error"))
        }
      },
      0,
      1,
      TimeUnit.SECONDS
    )

    val f = p.future

    f.onComplete { res =>
      println(s"completed $started with result: $res")
      scheduledFuture.cancel(true)
    }

    f
  }
}
