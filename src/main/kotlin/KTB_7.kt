import java.io.File
import java.util.Scanner

private const val LEARNED_THRESHOLD = 3

data class Word(
    val original: String,
    val translated: String,
    var correctAnswersCount: Int = 0
)

// --- Data Management ---
class DictionaryFileManager(private val filename: String = "words.txt") {
    fun loadDictionary(): List<Word> {
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

    fun saveDictionary(dictionary: List<Word>) {
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
}

class StatisticsCalculator {
    fun calculateStatistics(dictionary: List<Word>): String {
        val learnedWords = dictionary.filter { it.correctAnswersCount >= LEARNED_THRESHOLD }
        val totalCount = dictionary.size
        val learnedCount = learnedWords.size
        val percent = if (totalCount > 0) (learnedCount.toDouble() / totalCount * 100).toInt() else 0

        return "Выучено $learnedCount из $totalCount слов | $percent%\n"
    }
}

class QuestionGenerator {
    fun generateQuestion(dictionary: List<Word>): QuestionData? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_THRESHOLD }

        if (notLearnedList.isEmpty()) {
            return null
        }

        val questionWords = notLearnedList.shuffled().take(4)
        val correctAnswer = questionWords.random()

        return QuestionData(correctAnswer, questionWords.shuffled())
    }
}

data class QuestionData(val correctAnswer: Word, val answerOptions: List<Word>)

fun Word.isCorrectAnswer(userAnswer: Word): Boolean = this == userAnswer
fun List<Word>.displayOptions(): String =
    this.mapIndexed { index, word -> "${index + 1} - ${word.translated}" }.joinToString("\n")

class ConsoleUI(private val scanner: Scanner = Scanner(System.`in`)) {
    fun displayMenu(): String {
        return "Меню: 1 – Учить слова 2 – Статистика 0 – Выход"
    }

    fun getUserInput(prompt: String): String {
        print(prompt)
        return scanner.nextLine()
    }

    fun displayMessage(message: String) {
        println(message)
    }
}

fun main() {
    val fileManager = DictionaryFileManager()
    var dictionary: List<Word> = fileManager.loadDictionary()
    val statisticsCalculator = StatisticsCalculator()
    val questionGenerator = QuestionGenerator()
    val ui = ConsoleUI()
    val filename = "words.txt"

    while (true) {
        ui.displayMessage(ui.displayMenu())
        val input = ui.getUserInput("Выберите пункт меню: ")

        when (input) {
            "1" -> {
                while (true) {
                    val questionData = questionGenerator.generateQuestion(dictionary)

                    if (questionData == null) {
                        ui.displayMessage("Все слова в словаре выучены")
                        break
                    }

                    val (correctAnswer, answerOptions) = questionData
                    ui.displayMessage("\n${correctAnswer.original}:")
                    ui.displayMessage(answerOptions.displayOptions())
                    ui.displayMessage("----------\n0 - Меню")

                    val userAnswerInput = ui.getUserInput("Ваш ответ (0-${answerOptions.size}): ")

                    when (userAnswerInput) {
                        "0" -> break
                        else -> {
                            val userAnswerIndex = userAnswerInput.toIntOrNull()
                            if (userAnswerIndex != null && userAnswerIndex in 1..answerOptions.size) {
                                val userAnswer = answerOptions[userAnswerIndex - 1]

                                if (correctAnswer.isCorrectAnswer(userAnswer)) {
                                    ui.displayMessage("Правильно!")
                                    correctAnswer.correctAnswersCount++
                                    fileManager.saveDictionary(dictionary)
                                    dictionary = fileManager.loadDictionary()
                                } else {
                                    ui.displayMessage("Неправильно! ${correctAnswer.original} – это ${correctAnswer.translated}")
                                }
                            } else {
                                ui.displayMessage("Некорректный ввод. Пожалуйста, введите число от 0 до ${answerOptions.size}")
                            }
                        }
                    }
                }
            }

            "2" -> {
                ui.displayMessage(statisticsCalculator.calculateStatistics(dictionary))
            }

            "0" -> {
                ui.displayMessage("Выход из программы...")
                return
            }

            else -> ui.displayMessage("Предупреждение: Введите число 1, 2 или 0")
        }
    }
}