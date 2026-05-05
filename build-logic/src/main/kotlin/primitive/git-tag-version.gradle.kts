package primitive

import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.kotlin.dsl.of
import org.gradle.process.ExecOperations

val gitTagProvider = providers.of(GitTagValueSource::class) {
    parameters {
        // ここでリポジトリのルートディレクトリを渡す
        gitRoot.set(rootProject.layout.projectDirectory)
    }
}
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

// Gitコマンドを実行して最新タグを取得するValueSource
abstract class GitTagValueSource @Inject constructor(
    private val execOperations: ExecOperations,
) : ValueSource<String, GitTagValueSource.Params> {

    interface Params : ValueSourceParameters {
        // 実行ディレクトリをパラメータとして定義
        val gitRoot: DirectoryProperty
    }

    override fun obtain(): String = try {
        // 標準出力をキャプチャするためのByteArrayOutputStream
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        // git describe コマンドを実行
        val result = execOperations.exec {
            setWorkingDir(parameters.gitRoot.get().asFile)
            // commandLine("git", "tag", "--sort=-creatordate") // もし作成日時順の最新タグが良い場合
            commandLine("git", "describe", "--tags", "--abbrev=1", "--always")
            standardOutput = stdout
            // エラーが発生してもGradleビルドを止めないようにし、戻り値で判断
            errorOutput = stderr
            isIgnoreExitValue = true
        }


        val currentDir = parameters.gitRoot.get().asFile.absolutePath
        println("--- Git Debug Start ---")
        println("Current Working Dir: $currentDir")

        // 実際に Git が「ここがルートだ」と思っている場所を確認
        val rootCheck = ByteArrayOutputStream()
        execOperations.exec {
            workingDir = parameters.gitRoot.get().asFile
            commandLine("git", "rev-parse", "--show-toplevel")
            standardOutput = rootCheck
        }
        println("Git Root as seen by Git: ${rootCheck.toString().trim()}")

        // そもそもタグが存在しているか確認
        val tagsCheck = ByteArrayOutputStream()
        execOperations.exec {
            workingDir = parameters.gitRoot.get().asFile
            commandLine("git", "tag")
            standardOutput = tagsCheck
        }
        println("Visible tags: ${tagsCheck.toString().trim()}")
        println("--- Git Debug End ---")


        if (result.exitValue == 0) {
            // 成功したら標準出力をトリムして返す
            println("stdout.toString() ${stdout}")
            stdout.toString().trim().removePrefix("v")
        } else {
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
