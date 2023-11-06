package org.mikhailgolubtsov.asyncpolling.example

import org.mikhailgolubtsov.asyncpolling.executor.pollable.Pollable
import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.duration.Duration

case class TaskLikeExample(name: String, length: Duration, started: Option[Instant], err: Boolean)

object TaskLikeExample {
  implicit val implInstance: Pollable[TaskLikeExample] = new Pollable[TaskLikeExample] {
    override def start(a: TaskLikeExample): TaskLikeExample =
      a.started.fold(a.copy(started = Some(Instant.now())))(_ => a)

    override def status(a: TaskLikeExample): Pollable.PollableStatus =
      a.started.fold(Pollable.PollableStatus.NotStarted: Pollable.PollableStatus) { started =>
        if (a.err) {
          if (Instant.now().isAfter(started.plus(a.length.toSeconds / 2, ChronoUnit.SECONDS))) {
            println(s"check ${a.name} failure")
            Pollable.PollableStatus.Failure(new Exception("some_failure"))
          } else Pollable.PollableStatus.Pending
        } else {
          if (Instant.now().isAfter(started.plus(a.length.toSeconds, ChronoUnit.SECONDS))) {
            println(s"check ${a.name} success, duration: ${a.length.toSeconds} seconds")
            Pollable.PollableStatus.Success
          } else Pollable.PollableStatus.Pending
        }
      }

    override def name(a: TaskLikeExample): String = a.name
  }
}
