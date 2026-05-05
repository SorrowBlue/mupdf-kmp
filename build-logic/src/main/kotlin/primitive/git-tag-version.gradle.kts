package primitive

import java.io.ByteArrayOutputStream

private val gitTagProvider = providers.of(GitTagValueSource::class) {}
private val tag = checkNotNull(gitTagProvider.orNull) { "No git tag found." }
version = checkNotNull(formatVersion(tag)) { "git tag is not valid." }

interface GitTagParameters : ValueSourceParameters

abstract class GitTagValueSource @Inject constructor(
    private val execOperations: ExecOperations,
) : ValueSource<String, GitTagParameters> {

    override fun obtain(): String = try {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execOperations.exec {
            commandLine("git", "describe", "--tags", "--abbrev=1")
            standardOutput = stdout
            errorOutput = stderr
            isIgnoreExitValue = true
        }
        if (result.exitValue == 0) {
            stdout.toString().trim()
        } else {
            println("Warning: Could not get git tag. (Exit code: ${result.exitValue})")
            println("Warning: $stderr")
            "0.0.0-SNAPSHOT"
        }
    } catch (e: Exception) {
        println("Error: Failed to execute git command: ${e.message}")
        "0.0.0-SNAPSHOT"
    }
}

private fun formatVersion(input: String): String {
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
        logger.lifecycle("version format: $input -> $it")
    }
}
