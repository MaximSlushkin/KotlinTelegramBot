package org.example.dictionary_file

import java.io.File

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
                println("Вы выбрали пункт \"Учить слова\"")
            }

            "2" -> {
                println("Вы выбрали пункт \"Статистика\"")
            }

            "0" -> {
                println("Выход из программы")
                break
            }

            else -> println("Введите число 1, 2 или 0")
        }
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

    for (word in dictionary) {

        println("Слово: ${word.originalWord}, Перевод: ${word.translation}, Правильные ответы: ${word.correctAnswersCount}")

    }
    return dictionary
}