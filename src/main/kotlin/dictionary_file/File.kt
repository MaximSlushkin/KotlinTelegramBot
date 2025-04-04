package org.example.dictionary_file

import java.io.File

const val MIN_CORRECT_ANSWER = 3

data class Word(
    val originalWord: String,
    val translation: String,
    var correctAnswersCount: Int = 0
)

fun main() {

    val trainer = LearnWordsTrainer()

    while (true) {
        println("Меню:")
        println("1 - Учить слова")
        println("2 - Статистика")
        println("0 - Выход")

        val userInput = readln()

        when (userInput) {
            "1" -> {
                studyWords(trainer.dictionary)
            }

            "2" -> {
                displayStatistics(trainer.dictionary)
            }

            "0" -> {
                println("Выход из программы")
                break
            }

            else -> println("Введите число 1, 2 или 0")
        }
        println()
    }
}

fun displayStatistics(dictionary: List<Word>) {
    val totalCount = dictionary.size
    val learnedWords = dictionary.filter { it.correctAnswersCount >= MIN_CORRECT_ANSWER }
    val learnedCount = learnedWords.size
    val percent = if (totalCount > 0) {
        (learnedCount * 100) / totalCount
    } else {
        0
    }

    println("Выучено $learnedCount из $totalCount слов | $percent%")
}

fun studyWords(dictionary: List<Word>) {
    while (true) {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < MIN_CORRECT_ANSWER }

        if (notLearnedList.isEmpty()) {
            println("Все слова в словаре выучены!")
            return
        }

        val questionWords = notLearnedList.shuffled().take(4)
        val correctAnswer = questionWords.random()

        println()
        println(correctAnswer.originalWord + ":")

        val answers = mutableListOf(correctAnswer.translation)

        while (answers.size < 4) {
            val incorrectAnswer = dictionary.filter { it != correctAnswer }
                .shuffled()
                .take(1)
                .map { it.translation }
                .first()
            if (incorrectAnswer !in answers) {
                answers.add(incorrectAnswer)
            }
        }

        answers.shuffle()

        answers.forEachIndexed { index, answer ->
            println("${index + 1} - $answer")
        }

        println("----------")
        println("0 - Меню")

        val userAnswerInput = readLine()?.toIntOrNull()

        when {
            userAnswerInput == 0 -> return
            userAnswerInput != null && userAnswerInput in 1..4 -> {
                if (answers[userAnswerInput - 1] == correctAnswer.translation) {
                    println("Правильно!")
                    correctAnswer.correctAnswersCount++
//                    saveDictionary(dictionary) - здесь выдает ошибку !!!!!

                } else {
                    println("Неправильно! ${correctAnswer.originalWord} – это ${correctAnswer.translation}")
                }
            }

            else -> {
                println("Введите номер от 0 до 4.")
            }
        }
    }
}