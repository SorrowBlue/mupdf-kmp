package primitive

import java.io.ByteArrayOutputStream


afterEvaluate {
    runCatching {
        val gitTagProvider = providers.of(GitTagValueSource::class) {}
        val tag = checkNotNull(gitTagProvider.orNull) { "No git tag found." }
        version = checkNotNull(formatVersion(tag)) { "git tag is not valid." }
    }.onFailure {
        logger.lifecycle(it.message)
    }
}

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
            commandLine("git", "describe", "--tags", "--abbrev=1", "--always")
            standardOutput = stdout
            // エラーが発生してもGradleビルドを止めないようにし、戻り値で判断
            errorOutput = stderr
            isIgnoreExitValue = true
        }
        println("Git Stdout: $stdout")
        println("Git Stderr: ${stderr.toString().trim()}")
        if (result.exitValue == 0) {
            // 成功したら標準出力をトリムして返す
            stdout.toString().trim()
        } else {
            // gitコマンド失敗時 (タグがない、gitリポジトリでない等)
            println("Warning: Could not get git tag. (Exit code: ${result.exitValue})")
            "0.0.0-SNAPSHOT"
        }.also {
            println("---Debug start---")
            val stdout = ByteArrayOutputStream()
            val stderr = ByteArrayOutputStream()
            execOperations.exec {
                commandLine("pwd","&&", "git","log","--oneline","-n" ,"5" ,"&&", "git", "tag")
                standardOutput = stdout
                errorOutput = stderr
                isIgnoreExitValue = true
            }
            println("stdout=$stdout")
            println("----------------")
        }
    } catch (e: Exception) {
        // その他の予期せぬエラー
        println("Error: Failed to execute git command: ${e.message}")
        "0.0.0-SNAPSHOT"
    }
}

fun formatVersion(input: String): String {
    if (input.isEmpty()) return ""

    // git describe の形式 (タグ)-(コミット数)-g(ハッシュ) に一致するか確認
    // 例: v1.0.1-beta01-5-g3a91
    val describeRegex = """^(.+)-(\d+)-g([0-9a-f]+)$""".toRegex()
    val matchResult = describeRegex.find(input)

    return if (matchResult == null) {
        // --- タグぴったりの場合 ---
        // ルール: タグだけならvを取り除いた文字列を返す
        // (注: 例示では v1.0.0 -> v1.0.0 となっていたため、もしvを残したい場合は .removePrefix("v") を消してください)
        input.removePrefix("v")
    } else {
        // --- タグから離れている場合 ---
        val baseTag = matchResult.groupValues[1] // 例: v1.0.1-beta01 または v1.0.3

        // betaが含まれているか確認
        val betaRegex = """(.+-beta)(\d+)$""".toRegex()
        val betaMatch = betaRegex.find(baseTag)

        if (betaMatch != null) {
            // ケース: betaが含まれる場合 -> 数値をインクリメント
            val prefix = betaMatch.groupValues[1]
            val numberStr = betaMatch.groupValues[2]
            val nextNumber = numberStr.toInt() + 1
            // 元の桁数を維持して（例: 01 -> 02）フォーマット
            val formattedNumber = nextNumber.toString().padStart(numberStr.length, '0')

            "$prefix$formattedNumber-SNAPSHOT"
        } else {
            // ケース: betaが含まれない場合 -> -beta01-SNAPSHOT を付与
            "$baseTag-beta01-SNAPSHOT"
        }
    }.also {
        logger.lifecycle("#formatVersion $input -> $it")
    }
}
