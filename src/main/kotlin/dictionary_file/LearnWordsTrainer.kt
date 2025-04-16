package org.example.dictionary_file

import java.io.File

data class Word(
    val originalWord: String,
    val translation: String,
    var correctAnswersCount: Int = 0
)

data class Statistics(
    val learnedWords: Int,
    val totalCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(private var question: Question? = null) {
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val learnedWords = dictionary.filter { it.correctAnswersCount >= MIN_CORRECT_ANSWER }.size
        val totalCount = dictionary.size
        val percent = learnedWords * 100 / totalCount

        return Statistics(learnedWords, totalCount, percent)
    }

    fun getNextQuestion(): Question? {

        val notLearnedList = dictionary.filter { it.correctAnswersCount < MIN_CORRECT_ANSWER }
        val learnedList = dictionary.filter { it.correctAnswersCount >= MIN_CORRECT_ANSWER }

        if (notLearnedList.isEmpty()) return null

        var questionWords = notLearnedList.shuffled().take(ANSWER_OPTIONS)
        val correctAnswer = questionWords.random()
        val remainingOptionsCount = ANSWER_OPTIONS - questionWords.size
        if (remainingOptionsCount > 0) {
            val additionalWords = learnedList.shuffled().take(remainingOptionsCount)
            questionWords += additionalWords
        }

        val shuffledVariants = questionWords.shuffled()

        question = Question(
            variants = shuffledVariants,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerIndex = it.variants.indexOf(it.correctAnswer)

            if (correctAnswerIndex == userAnswerIndex) {
                if (it.correctAnswer.correctAnswersCount < MIN_CORRECT_ANSWER) {
                    it.correctAnswer.correctAnswersCount++
                    saveDictionary(dictionary)
                }
                true
            } else {
                false
            }
        } ?: false
    }

    fun loadDictionary(): MutableList<Word> {
        val wordsFile: File = File("word.txt")
        val dictionary = mutableListOf<Word>()

        val lines: List<String> = wordsFile.readLines()
        for (line in lines) {
            val parts = line.split("|")

            val word = parts[0]
            val translation = parts[1]
            val correctAnswersCount = parts.getOrNull(2)?.toIntOrNull() ?: 0

            val wordObject = Word(word, translation, correctAnswersCount)
            dictionary.add(wordObject)
        }
        return dictionary
    }

    fun saveDictionary(dictionary: List<Word>) {
        val wordsFile: File = File("word.txt")
        wordsFile.printWriter().use { out ->
            for (word in dictionary) {
                out.println("${word.originalWord}|${word.translation}|${word.correctAnswersCount}")
            }
        }
    }
}