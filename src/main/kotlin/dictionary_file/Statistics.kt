package org.example.dictionary_file

import kotlinx.serialization.Serializable

@Serializable
data class Statistics(
    val learnedWords: Int,
    val totalCount: Int,
    val percent: Int,
)