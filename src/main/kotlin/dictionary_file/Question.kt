package org.example.dictionary_file

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)