package script

import java.io.File
import java.nio.file.Paths
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.host.toScriptSource
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val path = Paths.get(object {}.javaClass.getResource("/example-test-implicit.kts").toURI()).toString()
    val scriptingHost = BasicHost()
    println("TOOK " + measureTimeMillis {
        val eval = scriptingHost.eval<TestScript>(File(path).toScriptSource())
        when (eval) {
            is ResultWithDiagnostics.Success -> {
                // FIXME: When I add "providedProperties" I can't run toString on "eval.value" or it throws StackOverflow.
                when (val result = eval.value.returnValue) {
                    is ResultValue.Value -> println(result.value)
                    is ResultValue.Unit -> TODO()
                    is ResultValue.Error -> println(result.error)
                    ResultValue.NotEvaluated -> TODO()
                }
            }
            is ResultWithDiagnostics.Failure -> println(eval.reports)
        }
    })
    println("TOOK " + measureTimeMillis {
        val eval = scriptingHost.eval<TestScript>(File(path).toScriptSource())
        when (eval) {
            is ResultWithDiagnostics.Success -> {
                // FIXME: When I add "providedProperties" I can't run toString on "eval.value" or it throws StackOverflow.
                when (val result = eval.value.returnValue) {
                    is ResultValue.Value -> println(result.value)
                    is ResultValue.Unit -> TODO()
                    is ResultValue.Error -> println(result.error)
                    ResultValue.NotEvaluated -> TODO()
                }
            }
            is ResultWithDiagnostics.Failure -> println(eval.reports)
        }
    })
}