package com.example.http4s.blaze.demo.server.service

import cats.effect.{ContextShift, Effect}
import com.example.http4s.blaze.demo.StreamUtils
import org.http4s._
import org.http4s.dsl.Http4sDsl
import fs2.Stream
import scala.concurrent.duration._

class WordSimilarityService[F[_]: ContextShift](implicit F: Effect[F], S: StreamUtils[F]) {

  /* script starts here */
  import cats.effect._
  import fs2._
  import java.nio.file.Paths
  import java.util.concurrent.Executors
  import scala.concurrent.ExecutionContext

  val blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  val src = io.file.readAll[F](java.nio.file.Paths.get("glove.6B.50d.txt"), blockingEc, 1024)

  val lines = src.through(text.utf8Decode).through(text.lines)
  val wordIndex = lines.map(_.split(" ").head).zipWithIndex
  def index(word: String): Stream[F, Long] = wordIndex.find(_._1.equals(word)).map(_._2)
  // get attributes for word
  def attribs(word:String) = index(word).flatMap(lines.drop(_).take(1)).map(_.split(" ").toVector).map(_.tail).map(_.map(_.toDouble))

  // vector calc
  def diffV(av: Vector[Double], bv: Vector[Double]): Vector[Double] =
    for ((a, b) <- av zip bv) yield (a - b)
  def dot(av: Vector[Double], bv: Vector[Double]): Double =
    (for ((a, b) <- av zip bv) yield a * b) sum
  def norm(av: Vector[Double]): Double =
    Math.sqrt((for (a <- av) yield a * a) sum)
  def cosine_similarity(a: Vector[Double], b: Vector[Double]): Double =
    dot(a, b) / (norm(a) * norm(b))

  // vector substraction
  def diff_word(u: String, v: String) = (attribs(u) zip attribs(v)).map(p => diffV(p._1.toVector, p._2.toVector))


  def similar_words(a: String, b: String, c: String, factor: Double): Stream[F, String]= {
    for {
      w <- wordIndex
      a_b <- diff_word(a, b)
      c_x <- diff_word(c, w._1)
      sim <- Stream(cosine_similarity(a_b, c_x)).filter(_ > factor)
    } yield (s"$w, $sim\n")

  }
}
