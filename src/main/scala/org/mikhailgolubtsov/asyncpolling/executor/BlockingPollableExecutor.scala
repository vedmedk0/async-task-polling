package org.mikhailgolubtsov.asyncpolling.executor

import org.mikhailgolubtsov.asyncpolling.executor.pollable.Pollable
import scala.concurrent.{ExecutionContext, Future}

class BlockingPollableExecutor[A: Pollable](implicit ec: ExecutionContext) extends PollableExecutor[A] {
  override def execute(pollable: A): Future[Pollable.PollableStatus] = Future {
    val started = Pollable[A].start(pollable)
    while (Pollable[A].status(started) == Pollable.PollableStatus.Pending) {
      println(s"pending for ${Pollable[A].name(pollable)}")
      Thread.sleep(1000)
    }
    Pollable[A].status(pollable)
  }
}
