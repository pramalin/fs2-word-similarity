Script to calculate word similarity from GloVe file.

To run, download [glove.6B.50d.txt](https://www.kaggle.com/watts2/glove6b50dtxt)

From Ammonite REPL.
```scala
@ import $file.read_glove, read_glove._

@ a
res1: String = "italy"
@ b
res2: String = "italian"
@ c
res3: String = "spain"
@ c_sim
res4: fs2.Stream[cats.effect.IO[x], (String, Double)] = Stream(..)
@ factor
res5: Double = 0.4
@ c_sim.take(10).compile.toVector.unsafeRunSync
```
__results__
```sh
@ c_sim.take(10).compile.toVector.unsafeRunSync
res1: Vector[(String, Double)] = Vector(
  ("french", 0.6347354522189068),
  ("english", 0.5219710309872815),
  ("italian", 0.7283498794430515),
  ("spanish", 0.8875303721276963),
  ("professional", 0.5020698039090873),
  ("dutch", 0.5002985942558867),
  ("mexican", 0.5790748083609243),
  ("brazilian", 0.5498009970871708),
  ("journalist", 0.528053034651764),
  ("portuguese", 0.644859697871396)

@ val factor = 0.8
@ c_sim.take(1).compile.toVector.unsafeRunSync
(spanish,1141), 0.8875303721276963
res4: Vector[(String, Double)] = Vector(("spanish", 0.8875303721276963))
```
Try with different words and factors.
This can be extremely slow.

####Word pair suggestions 
- ("italy", "italian", "spain"),
- ("india", "delhi", "japan"),
- ("man", "woman", "boy"),
- ("small", "smaller", "large")
