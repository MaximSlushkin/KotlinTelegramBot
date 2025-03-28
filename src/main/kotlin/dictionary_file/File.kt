package org.example.dictionary_file

import java.io.File

data class Word(
    val originalWord: String,
    val translation: String,
    var correctAnswersCount: Int = 0
)

fun main() {
    val wordsFile: File = File("word.txt")

    val dictionary = mutableListOf<Word>()

    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {

        val parts = line.split("|")

        val word = parts[0]
        val translation = parts[1]

        val correctAnswersCount = parts.getOrNull(2)?.toInt() ?: 0

        val wordObject = Word(word, translation, correctAnswersCount)
        dictionary.add(wordObject)
    }

    for (word in dictionary) {

        println("Слово: ${word.originalWord}, Перевод: ${word.translation}, Правильные ответы: ${word.correctAnswersCount}")

    }
}