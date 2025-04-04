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

        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val notLearnedList = trainer.dictionary.filter { it.correctAnswersCount < MIN_CORRECT_ANSWER }

                    if (notLearnedList.isEmpty()) {
                        println("Все слова выучены!")
                        break
                    } else {
                        val questionWords = notLearnedList.take(4).shuffled()
                        val correctAnswer = questionWords.random()

                        println(
                            "${correctAnswer.originalWord}\n" +
                                    "1 - ${questionWords[0].translation}\n2 - ${questionWords[1].translation}\n" +
                                    "3 - ${questionWords[2].translation}\n4 - ${questionWords[3].translation}\n" +
                                    "----------\n" +
                                    "0 - В меню"
                        )

                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == 0) break
                        val correctAnswerIndex = questionWords.indexOf(correctAnswer)

                        if (userAnswerInput == correctAnswerIndex + 1) {
                            correctAnswer.correctAnswersCount++
                            trainer.saveDictionary(trainer.dictionary)
                            println("Правильно!\n")
                        } else {
                            println("Неправильно! ${correctAnswer.originalWord} - это ${correctAnswer.translation}\n")
                        }
                    }
                }
            }

            2 -> {
            val statistics = trainer.getStatistics()
                println("Выучено ${statistics.learnedWords} из ${statistics.totalCount} слов | ${statistics.percent}%")
            }

            0 -> {
                println("Выход из программы")
                break
            }

            else -> println("Введите число 1, 2 или 0")
        }
    }
}