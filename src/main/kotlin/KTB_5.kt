import java.io.File
import java.util.Scanner

data class Word(
    val original: String,
    val translated: String,
    var correctAnswersCount: Int = 0
)

fun loadDictionary(): List<Word> {
    val filename = "words.txt"
    val dictionary = mutableListOf<Word>()
    try {
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
    } catch (e: Exception) {
        println("Ошибка при чтении файла: ${e.message}")
        return emptyList()
    }
    return dictionary.toList()
}

fun main() {
    val dictionary: List<Word> = loadDictionary()
    val scanner = Scanner(System.`in`)
    while (true) {
        println("Меню: 1 – Учить слова 2 – Статистика 0 – Выход")
        print("Выберите пункт меню: ")
        val input = scanner.nextLine()
        when (input) {
            "1" -> println("Вы выбрали пункт 'Учить слова'")
            "2" -> println("Вы выбрали пункт 'Статистика'")
            "0" -> {
                println("Выход из программы...")
                return
            }

            else -> println("Предупреждение: Введите число 1, 2 или 0")
        }
    }
}