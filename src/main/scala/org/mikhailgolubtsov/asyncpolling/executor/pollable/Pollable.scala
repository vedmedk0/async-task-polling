package org.mikhailgolubtsov.asyncpolling.executor.pollable

trait Pollable[A] {
  def name(a: A): String

  def start(a: A): A // returns instance of a started task

  def status(a: A): Pollable.PollableStatus

}

object Pollable {

  def apply[A: Pollable]: Pollable[A] = implicitly[Pollable[A]]

  sealed trait PollableStatus

  object PollableStatus {

    case object NotStarted extends PollableStatus

    case object Success extends PollableStatus

    case object Pending extends PollableStatus

    case class Failure(t: Throwable) extends PollableStatus
  }
}
