import java.io.File

fun main() {
    val wordsFile = "words.txt"
    File(wordsFile).apply {
        if (!exists()) {
            createNewFile()
            writeText(
                """
                hello привет
                dog собака
                cat кошка
       """.trimIndent()
            )
        }
    }
    val lines: List<String> = File(wordsFile).readLines()
    println("Содержимое файла $wordsFile:")
    for (line in lines) {
        println(line)
    }
}