import java.io.File

data class Word(
    val original: String,
    val translated: String,
    var correctAnswersCount: Int = 0
)

fun main() {
    val filename = "words.txt"
    val dictionary: MutableList<Word> = mutableListOf()
    File(filename).forEachLine { line ->
        val parts = line.split("|").map { it.trim() }
        if (parts.size < 2) {
            println("Ошибка: Некорректный формат строки (отсутствует разделитель '|'): $line")
            return@forEachLine
        }
        val original = parts[0]
        val translated = parts[1]
        val correctAnswersCount = parts.getOrNull(2)?.toIntOrNull() ?: 0
        dictionary.add(Word(original, translated, correctAnswersCount))
    }
    dictionary.forEach { println("Original: ${it.original}, Translated: ${it.translated}, Correct Answers: ${it.correctAnswersCount}") }
}