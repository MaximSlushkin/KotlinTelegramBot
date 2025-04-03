package org.example.dictionary_file

import java.io.File

const val MIN_CORRECT_ANSWER = 3

data class Word(
    val originalWord: String,
    val translation: String,
    var correctAnswersCount: Int = 0
)

fun main() {
    val dictionary = loadDictionary()

    while (true) {
        println("Меню:")
        println("1 - Учить слова")
        println("2 - Статистика")
        println("0 - Выход")

        val userInput = readln()

        when (userInput) {
            "1" -> {
                studyWords(dictionary)
            }

            "2" -> {
                displayStatistics(dictionary)
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

        // Проверка ввода
        when {
            userAnswerInput == 0 -> return // Возврат в меню
            userAnswerInput != null && userAnswerInput in 1..4 -> {
                if (answers[userAnswerInput - 1] == correctAnswer.translation) {
                    println("Правильно!")
                    correctAnswer.correctAnswersCount++
                    saveDictionary(dictionary) // Сохраняем прогресс
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

fun saveDictionary(dictionary: List<Word>) {
    val wordsFile: File = File("word.txt")
    wordsFile.printWriter().use { out ->
        for (word in dictionary) {
            out.println("${word.originalWord}|${word.translation}|${word.correctAnswersCount}")
        }
    }
}