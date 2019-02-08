/* jar imports */
import $ivy.`co.fs2::fs2-core:1.0.2`
import $ivy.`co.fs2::fs2-io:1.0.2`

// disable REPL printing verbose IO values 
import cats.effect._

def disablePrettyPrintIO = repl.pprinter.update(repl.pprinter().copy(additionalHandlers = {
  case io: cats.effect.IO[_] => pprint.Tree.Literal("✨✨✨")
}))

disablePrettyPrintIO

def enablePrettyPrintIO = repl.pprinter.update(repl.pprinter().copy(additionalHandlers = PartialFunction.empty))

/* script starts here */
import cats.effect._
import fs2._
import java.nio.file.Paths
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)
implicit val timer: Timer[IO] = IO.timer(scala.concurrent.ExecutionContext.Implicits.global)

val blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
val src = io.file.readAll[IO](java.nio.file.Paths.get("glove.6B.50d.txt"), blockingEc, 1024)

val lines = src.through(text.utf8Decode).through(text.lines)
val wordIndex = lines.map(_.split(" ").head).zipWithIndex
def index(word: String): Stream[IO, Long] = wordIndex.find(_._1.equals(word)).map(_._2)
// get attributes for word
def attribs(word:String) = index(word).flatMap(lines.drop(_).take(1)).map(_.split(" ").toVector).map(_.tail).map(_.map(_.toDouble)) 

// test index
index("queen").flatMap(lines.drop(_)).take(1)

// vector calc
def diffV(av: Vector[Double], bv: Vector[Double]): Vector[Double] =
  for ((a, b) <- av zip bv) yield (a - b)
def dot(av: Vector[Double], bv: Vector[Double]): Double =
  (for ((a, b) <- av zip bv) yield a * b) sum
def norm(av: Vector[Double]): Double =
  Math.sqrt((for (a <- av) yield a * a) sum)
def cosine_similarity(a: Vector[Double], b: Vector[Double]): Double =
   dot(a, b) / (norm(a) * norm(b))



// get distance between words
def dist(u: String, v: String) = (attribs(u) zip attribs(v)).map(p => cosine_similarity(p._1.toVector, p._2.toVector)) 

//dist("him", "her").compile.toVector.unsafeRunSync 
//dist("king", "queen").compile.toVector.unsafeRunSync 
//dist("man", "woman").compile.toVector.unsafeRunSync 
// ("italy", "italian", "spain"), ("india", "delhi", "japan"), ("man", "woman", "boy"), ("small", "smaller", "large")
val a = "italy"
val b = "italian"
val c = "spain"
val factor = 0.4

// word distance
def diff_word(u: String, v: String) = (attribs(u) zip attribs(v)).map(p => diffV(p._1.toVector, p._2.toVector)) 

val c_sim = for {
  w <- wordIndex
  a_b <- diff_word(a, b)
  c_x <- diff_word(c, w._1)
  sim <- Stream(cosine_similarity(a_b, c_x))
  _ <- if (sim > factor) Stream.eval(IO(println(s"$w, $sim"))) else Stream()
} yield (w._1, sim)

/*
// results
@ c_sim.take(10).compile.toVector.unsafeRunSync
(a,7), 0.40563150208976145
(an,29), 0.4252293693397883
(american,140), 0.42150345258791344
(known,225), 0.4203211204887907
(british,297), 0.42361789696212426
(french,348), 0.6347354522189068
(russian,467), 0.46762615979965744
(whose,507), 0.4229735137631385
(german,514), 0.46345247634759656
(named,564), 0.42304541430647663
@ val factor = 0.8
@ c_sim.take(1).compile.toVector.unsafeRunSync
(spanish,1141), 0.8875303721276963
res4: Vector[(String, Double)] = Vector(("spanish", 0.8875303721276963))
*/
