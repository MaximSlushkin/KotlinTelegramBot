package org.example.dictionary_file

fun Question.asConsoleString(): String {

    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> "${index + 1} - ${word.translation}"}
        .joinToString(separator = "\n")
    return this.correctAnswer.originalWord + "\n"+ variants + "\n-----------------" + "\n0 - Выйти в меню"
}

fun main() {

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь!")
        return
    }

    LearnWordsTrainer()

    while (true) {
        println("Меню:")
        println("1 - Учить слова")
        println("2 - Статистика")
        println("0 - Выход")

        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Все слова выучены!")
                        break
                    }

                   println(question.asConsoleString())

                    val userAnswerInput = readln().toIntOrNull()
                    if (userAnswerInput == 0) break
                    if (trainer.checkAnswer(userAnswerInput?.minus(1))){
                        println("Правильно")
                    } else {
                        println("Неправильно! ${question.correctAnswer.originalWord} - это ${question.correctAnswer.translation}")
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