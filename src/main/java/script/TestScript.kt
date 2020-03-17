@file:JvmName("TestImplicitKt")

package script

import java.io.File
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.api.scriptsInstancesSharing
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache

const val COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR = "KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR"
const val COMPILED_SCRIPTS_CACHE_DIR_PROPERTY = "kotlin.main.kts.compiled.scripts.cache.dir"

// Maybe add compilation/runtime parameters to this annotation as well?
@KotlinScript(
    displayName = "Test implicit file",
    filePathPattern = ".*-test-implicit.kts",
    compilationConfiguration = TestScriptCompilationConfiguration::class,
    evaluationConfiguration = TestScriptEvaluationConfiguration::class
)
abstract class TestScript {
    fun test(init: TestImplicit.() -> Unit): TestImplicit {
        val testImplicit = TestImplicit("test")
        testImplicit.init()
        return testImplicit
    }
}

object TestScriptCompilationConfiguration : ScriptCompilationConfiguration({
    defaultImports(
        LocalDateTime::class.qualifiedName!!,
        LocalDate::class.qualifiedName!!
    )

    jvm {
        dependenciesFromClassContext(TestScriptCompilationConfiguration::class, wholeClasspath = true)
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }

    // The below works
    implicitReceivers(
        Implicit::class
    )

    // Reflection works too !
    // val implicitClass = try {
    //     Class.forName("script.Implicit").kotlin
    // } catch (e: ClassNotFoundException) {
    //     null
    // }
    //
    // if (implicitClass != null) {
    //     implicitReceivers(
    //         implicitClass
    //     )
    // }

    // FIXME Cache does not work well as implicit receivers and are added to constructor parameters.
    hostConfiguration(ScriptingHostConfiguration {
        jvm {
            val cacheExtSetting = System.getProperty(COMPILED_SCRIPTS_CACHE_DIR_PROPERTY)
                ?: System.getenv(COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR)
            val cacheBaseDir = when {
                cacheExtSetting == null -> System.getProperty("java.io.tmpdir")
                    ?.let(::File)?.takeIf { it.exists() && it.isDirectory }
                    ?.let { File(it, "main.kts.compiled.cache").apply { mkdir() } }
                cacheExtSetting.isBlank() -> null
                else -> File(cacheExtSetting)
            }?.takeIf { it.exists() && it.isDirectory }
            if (cacheBaseDir != null) {
                compilationCache(
                    CompiledScriptJarsCache { script, scriptCompilationConfiguration ->
                        File(cacheBaseDir, compiledScriptUniqueName(script, scriptCompilationConfiguration) + ".jar")
                    }
                )
            }
        }
    })
})

object TestScriptEvaluationConfiguration : ScriptEvaluationConfiguration({
    scriptsInstancesSharing(true)

    // Actual implicit value
    implicitReceivers(
        Implicit
    )

    // Reflection works too !
    // val implicitClass = try {
    //     Class.forName("script.Implicit").kotlin
    // } catch (e: ClassNotFoundException) {
    //     null
    // }
    //
    // if (implicitClass != null) {
    //     implicitReceivers(
    //         implicitClass.objectInstance!!
    //     )
    // }
})

private fun compiledScriptUniqueName(
    script: SourceCode,
    scriptCompilationConfiguration: ScriptCompilationConfiguration
): String {
    val digestWrapper = MessageDigest.getInstance("MD5")
    digestWrapper.update(script.text.toByteArray())
    scriptCompilationConfiguration.notTransientData.entries
        .sortedBy { it.key.name }
        .forEach {
            digestWrapper.update(it.key.name.toByteArray())
            digestWrapper.update(it.value.toString().toByteArray())
        }
    return digestWrapper.digest().toHexString()
}

private fun ByteArray.toHexString(): String = joinToString("", transform = { "%02x".format(it) })