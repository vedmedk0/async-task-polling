package org.mikhailgolubtsov.asyncpolling.executor

import org.mikhailgolubtsov.asyncpolling.executor.pollable.Pollable
import scala.concurrent.Future

trait PollableExecutor[A] {
  def execute(pollable: A): Future[Pollable.PollableStatus]
}
