// interp.load.ivy("co.fs2" %% "fs2-core" % "1.0.2")
// interp.load.ivy("co.fs2" %% "fs2-io" % "1.0.2")

// deep learning 4 java
// interp.load.ivy("org.nd4j" % "nd4j-native-platform" % "1.0.0-beta3")
// interp.load.ivy("org.nd4j" %% "nd4s" % "1.0.0-beta3")
// interp.load.ivy("org.nd4j" % "nd4s_2.12.0-M3" % "0.4-rc3.8") 


// interp.configureCompiler(_.settings.YpartialUnification.value = true)

import cats.effect._

def disablePrettyPrintIO = repl.pprinter.update(repl.pprinter().copy(additionalHandlers = {
  case io: cats.effect.IO[_] => pprint.Tree.Literal("✨✨✨")
}))

disablePrettyPrintIO

def enablePrettyPrintIO = repl.pprinter.update(repl.pprinter().copy(additionalHandlers = PartialFunction.empty))
