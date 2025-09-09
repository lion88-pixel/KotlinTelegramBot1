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
            "1" -> {
                while (true) {
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < 3 }
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
                    print("Ваш ответ (1-${answerOptions.size}): ")
                    val userAnswerInput = scanner.nextLine()
                    val userAnswerIndex = userAnswerInput.toIntOrNull()
                    if (userAnswerIndex != null && userAnswerIndex in 1..answerOptions.size) {
                        val userAnswer = answerOptions[userAnswerIndex - 1]

                        if (userAnswer == correctAnswer) {
                            println("Правильно!")
                            correctAnswer.correctAnswersCount++
                        } else {
                            println("Неправильно. Правильный ответ: ${correctAnswer.translated}")
                        }
                    } else {
                        println("Некорректный ввод. Пожалуйста, введите число от 1 до ${answerOptions.size}")
                    }
                }
            }

            "2" -> {
                val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }
                val totalCount = dictionary.size
                val learnedCount = learnedWords.size
                val percent = if (totalCount > 0) (learnedCount.toDouble() / totalCount * 100).toInt() else 0
                println("Выучено $learnedCount из $totalCount слов | $percent%")
                println()
            }

            "0" -> {
                println("Выход из программы...")
                return
            }

            else -> println("Предупреждение: Введите число 1, 2 или 0")
        }
    }
}