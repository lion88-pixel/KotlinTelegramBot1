import java.io.File
import java.lang.IllegalStateException

data class Statistics(
    val learned: Int,
    val total: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(val learnedAnswerCount: Int = 3, private val countOfQuestionWords: Int = 4) {

    private var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val total = dictionary.size
        val learned = dictionary.count { it.correctAnswersCount >= learnedAnswerCount }
        val percent = if (total > 0)
            learned * 100 / total else 0
        return Statistics(learned, total, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
        if (notLearnedList.isEmpty()) return null
        val questionWord = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.shuffled()
            notLearnedList.shuffled().take(countOfQuestionWords) +
                    learnedList.take(countOfQuestionWords - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(countOfQuestionWords)
        }.shuffled()

        val correctAnswer = questionWord.random()

        question = Question(
            variants = questionWord,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        try {
            val dictionary = mutableListOf<Word>()
            val wordsFile = File("words.txt")
            wordsFile.readLines().forEach {
                val splitLine = it.split("|")
                dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("некорректный файл")
        }
    }

    private fun saveDictionary(words: List<Word>) {
        val wordsFile = File("words.txt")
        wordsFile.printWriter().use { out ->
            words.forEach { word ->
                out.println(
                    "${word.questionWord}|${word.translate}|" +
                            "${word.correctAnswersCount}\n"
                )
            }
        }
    }
}