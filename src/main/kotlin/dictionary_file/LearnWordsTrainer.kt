package org.example.dictionary_file

import org.example.constants.*
import java.io.File

class LearnWordsTrainer(
    private val fileName: String = DEFAULT_DICTIONARY,
    val answerOptions: Int = 4,
    val minCorrectAnswer: Int = 3,
) {

    private val dictionary = loadDictionary()
    var question: Question? = null

    fun getStatistics(): Statistics {
        val learnedWords = dictionary.filter { it.correctAnswersCount >= minCorrectAnswer }.size
        val totalCount = dictionary.size
        val percent = learnedWords * 100 / totalCount

        return Statistics(learnedWords, totalCount, percent)
    }

    fun getNextQuestion(): Question? {

        val notLearnedList = dictionary.filter { it.correctAnswersCount < minCorrectAnswer }

        if (notLearnedList.isEmpty()) return null

        var questionWords = notLearnedList.shuffled().take(answerOptions)
        val correctAnswer = questionWords.random()
        val remainingOptionsCount = answerOptions - questionWords.size
        if (remainingOptionsCount > 0) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= minCorrectAnswer }
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

                if (it.correctAnswer.correctAnswersCount < minCorrectAnswer) {
                    it.correctAnswer.correctAnswersCount++
                    saveDictionary()
                }
                true
            } else {
                false
            }
        } ?: false
    }

    fun resetStatistics() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }

    private fun loadDictionary(): List<Word> {
        val wordsFile: File = File(fileName)
        if (!wordsFile.exists()) {
            File(DEFAULT_DICTIONARY).copyTo(wordsFile)
        }

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

    private fun saveDictionary() {
        val wordsFile: File = File(fileName)
        wordsFile.printWriter().use { out ->
            for (word in dictionary) {
                out.println("${word.originalWord}|${word.translation}|${word.correctAnswersCount}")
            }
        }
    }
}