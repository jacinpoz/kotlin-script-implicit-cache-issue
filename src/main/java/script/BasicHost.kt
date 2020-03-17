package script

import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class BasicHost {

    val scriptingHost = BasicJvmScriptingHost()

    inline fun <reified O : Any> eval(
        sourceCode: SourceCode,
        noinline compilation: ScriptCompilationConfiguration.Builder.() -> Unit = {},
        noinline evaluation: ScriptEvaluationConfiguration.Builder.() -> Unit = {}
    ): ResultWithDiagnostics<EvaluationResult> =
        scriptingHost.evalWithTemplate<O>(
            sourceCode,
            compilation,
            evaluation
        )

    inline fun <reified O : Any> eval(
        scriptName: String,
        sourceCode: String,
        noinline compilation: ScriptCompilationConfiguration.Builder.() -> Unit = {},
        noinline evaluation: ScriptEvaluationConfiguration.Builder.() -> Unit = {}
    ): ResultWithDiagnostics<EvaluationResult> =
        scriptingHost.evalWithTemplate<O>(
            sourceCode.toScriptSource(scriptName),
            compilation,
            evaluation
        )
}
