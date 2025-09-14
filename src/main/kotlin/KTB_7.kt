import java.io.File
import java.util.Scanner

data class Word(
    val original: String,
    val translated: String,
    var correctAnswersCount: Int = 0
)

private const val LEARNED_THRESHOLD = 3

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

fun saveDictionary(dictionary: List<Word>, filename: String = "words.txt") {
    try {
        File(filename).printWriter().use { out ->
            dictionary.forEach { word ->
                out.println("${word.original}|${word.translated}|${word.correctAnswersCount}")
            }
        }
    } catch (e: Exception) {
        println("Ошибка при сохранении словаря: ${e.message}")
    }
}

fun main() {
    var dictionary: List<Word> = loadDictionary()
    val scanner = Scanner(System.`in`)
    val filename = "words.txt"
    while (true) {
        println("Меню: 1 – Учить слова 2 – Статистика 0 – Выход")
        print("Выберите пункт меню: ")
        val input = scanner.nextLine()
        when (input) {
            "1" -> {
                while (true) {
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_THRESHOLD }
                    if (notLearnedList.isEmpty()) {
                        println("Все слова в словаре выучены")
                        break
                    }

                    val questionWords = notLearnedList.shuffled().take(4)
                    val correctAnswer = questionWords.random()
                    println()
                    println("${correctAnswer.original}:")
                    val answerOptions = questionWords.shuffled()
                    for (i in answerOptions.indices) {
                        println("${i + 1} - ${answerOptions[i].translated}")
                    }
                    println("----------")
                    println("0 - Меню")
                    print("Ваш ответ (0-${answerOptions.size}): ")
                    val userAnswerInput = scanner.nextLine()
                    when (userAnswerInput) {
                        "0" -> break
                        else -> {
                            val userAnswerIndex = userAnswerInput.toIntOrNull()
                            if (userAnswerIndex != null && userAnswerIndex in 1..answerOptions.size) {
                                val userAnswer = answerOptions[userAnswerIndex - 1]

                                if (userAnswer == correctAnswer) {
                                    println("Правильно!")
                                    correctAnswer.correctAnswersCount++
                                    saveDictionary(dictionary, filename)
                                } else {
                                    println("Неправильно! ${correctAnswer.original} – это ${correctAnswer.translated}")
                                }
                            } else {
                                println("Некорректный ввод. Пожалуйста, введите число от 0 до ${answerOptions.size}")
                            }
                        }
                    }
                }
            }

            "2" -> {
                val learnedWords = dictionary.filter { it.correctAnswersCount >= LEARNED_THRESHOLD }
                val totalCount = dictionary.size
                val learnedCount = learnedWords.size
                val percent = if (totalCount > 0) (learnedCount.toDouble() / totalCount * 100).toInt() else 0
                println("Выучено $learnedCount из $totalCount слов | $percent%\n")
            }

            "0" -> {
                println("Выход из программы...")
                return
            }

            else -> println("Предупреждение: Введите число 1, 2 или 0")
        }
    }
}