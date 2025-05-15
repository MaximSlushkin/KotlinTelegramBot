package org.example.dictionary_file

import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val originalWord: String,
    val translation: String,
    var correctAnswersCount: Int = 0
)