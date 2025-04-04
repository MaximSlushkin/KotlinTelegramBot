package org.example.dictionary_file

import java.io.File

class LearnWordsTrainer {

    val dictionary = loadDictionary()

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