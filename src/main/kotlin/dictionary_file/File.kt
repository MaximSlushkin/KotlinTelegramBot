package org.example.dictionary_file

import java.io.File

fun main() {

    val wordsFile: File = File("word.txt")

    wordsFile.createNewFile()
    wordsFile.writeText("hello привет")
    wordsFile.appendText("\ndog собака")
    wordsFile.appendText("\ncat кошка")

    println(wordsFile.readLines())

}