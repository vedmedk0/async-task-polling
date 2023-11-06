package org.mikhailgolubtsov.asyncpolling.example

import org.mikhailgolubtsov.asyncpolling.executor.pollable.Pollable
import org.mikhailgolubtsov.asyncpolling.executor.{
  BlockingPollableExecutor,
  NonBlockingPollableExecutor,
  PollableExecutor
}

import java.time.Instant
import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Random

object BenchmarkApp extends App {
  val rnd = new Random()

  val tasks =
    for (i <- 1 to 50) yield TaskLikeExample(i.toString, (rnd.nextInt(6) + 5).seconds, None, rnd.nextBoolean())

  def measured(code: => Unit): Unit = {
    val start = Instant.now()
    code
    val end = Instant.now()
    println(s"end in ${end.getEpochSecond - start.getEpochSecond} seconds")
  }

  def scenario1(): Unit = {
    val es          = Executors.newFixedThreadPool(4)
    implicit val ec = ExecutionContext.fromExecutor(es)

    val exec: PollableExecutor[TaskLikeExample] = new BlockingPollableExecutor()

    val res = tasks.map(exec.execute)

    val awaitRes = Await.result(Future.sequence(res), Duration.Inf)
    println(awaitRes)

    es.shutdown()
    val terminated = es.awaitTermination(1, TimeUnit.SECONDS)
    println(terminated)

    println("TASKS - NOT STARTED")
    println("SERVICE - FIXED THREAD POOL 4")
    println("EXECUTOR - BLOCKING")
  }

  def scenario6(): Unit = {
    val es          = Executors.newSingleThreadScheduledExecutor()
    implicit val ec = ExecutionContext.fromExecutor(es)

    val exec: PollableExecutor[TaskLikeExample] = new NonBlockingPollableExecutor(es)

    val res = tasks.map(exec.execute)

    val awaitRes = Await.result(Future.sequence(res), Duration.Inf)
    println(awaitRes)

    es.shutdown()
    val terminated = es.awaitTermination(1, TimeUnit.SECONDS)
    println(terminated)

    println("TASKS - NOT STARTED")
    println("SERVICE - SCHDEULED 1 THREAD")
    println("EXECUTOR - NON-BLOCKING")
  }

  def scenario12(): Unit = {
    val es          = Executors.newSingleThreadExecutor()
    implicit val ec = ExecutionContext.fromExecutor(es)

    val exec: PollableExecutor[TaskLikeExample] = new BlockingPollableExecutor()

    val res = tasks.map(t => exec.execute(Pollable[TaskLikeExample].start(t)))

    val awaitRes = Await.result(Future.sequence(res), Duration.Inf)
    println(awaitRes)

    es.shutdown()
    val terminated = es.awaitTermination(1, TimeUnit.SECONDS)
    println(terminated)

    println("TASKS - STARTED")
    println("SERVICE - FIXED THREAD POOL 1")
    println("EXECUTOR - BLOCKING")
  }

  measured(scenario1())
//  measured(scenario6())
//  measured(scenario12())
  println(s"NUMBER OF TASKS: ${tasks.length}")

}
