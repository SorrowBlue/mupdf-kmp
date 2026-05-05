package primitive

import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.kotlin.dsl.of
import org.gradle.process.ExecOperations

val gitTagProvider = providers.of(GitTagValueSource::class) {}
runCatching {
    println("${rootProject.projectDir}")
    val tag = checkNotNull(gitTagProvider.orNull) { "No git tag found." }
    version =
        checkNotNull(
            releaseVersionOrSnapshot(tag.removePrefix("v")),
        ) { "git tag is not valid." }
    logger.lifecycle("version=$version")
}.onFailure {
    logger.warn(it.message)
    logger.warn("Failed to get git tag. Using version 'UNKNOWN'.")
    version = "0.0.0-SNAPSHOT"
}

// パラメータは不要だが、インターフェースとして定義が必要
interface GitTagParameters : ValueSourceParameters

// Gitコマンドを実行して最新タグを取得するValueSource
abstract class GitTagValueSource @Inject constructor(
    private val execOperations: ExecOperations,
) : ValueSource<String, GitTagParameters> {

    override fun obtain(): String = try {
        // 標準出力をキャプチャするためのByteArrayOutputStream
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        // git describe コマンドを実行
        val result = execOperations.exec {
            // commandLine("git", "tag", "--sort=-creatordate") // もし作成日時順の最新タグが良い場合
            commandLine("git", "describe", "--tags", "--abbrev=1")
            standardOutput = stdout
            // エラーが発生してもGradleビルドを止めないようにし、戻り値で判断
            errorOutput = stderr
            isIgnoreExitValue = true
            // エラー出力は捨てる (必要ならキャプチャも可能)
            errorOutput = ByteArrayOutputStream()
        }

        if (result.exitValue == 0) {
            // 成功したら標準出力をトリムして返す
            stdout.toString().trim().removePrefix("v")
        } else {
            println("--- Git Debug Info ---")
            // タグが一つでも見えているか確認
            val tagCheck = java.io.ByteArrayOutputStream()
            execOperations.exec {
                commandLine("git", "tag")
                standardOutput = tagCheck
                isIgnoreExitValue = true
            }
            println("Available tags: ${tagCheck.toString()}")
            println("-----------------------")
            // gitコマンド失敗時 (タグがない、gitリポジトリでない等)
            println("Git Error Output: ${stderr.toString().trim()}")
            println("Warning: Could not get git tag. (Exit code: ${result.exitValue})")
            "0.0.0-SNAPSHOT"
        }
    } catch (e: Exception) {
        // その他の予期せぬエラー
        println("Error: Failed to execute git command: ${e.message}")
        "0.0.0-SNAPSHOT"
    }
}

private fun releaseVersionOrSnapshot(tag: String): String? {
    val regex = Regex("""(^\d+\.\d+\.)(\d+)([\w-]*)$""")
    val groups = regex.find(tag)?.groups ?: return null
    return if (groups.size == 4) {
        if (groups[3]?.value?.isEmpty() == true) {
            requireNotNull(groups.first()).value
        } else {
            "${requireNotNull(
                groups[1]
            ).value}${requireNotNull(groups[2]).value.toInt().plus(1)}-SNAPSHOT"
        }
    } else {
        null
    }
}
