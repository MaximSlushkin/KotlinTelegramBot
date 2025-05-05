package org.example

import org.example.dictionary_file.LearnWordsTrainer
import org.example.dictionary_file.TelegramBotService

const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val OPEN_MENU = "menu"

fun main(args: Array<String>) {

    val botToken = args[0]
    val botService = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()
    var updateId = 0

    val regexFindUpdateId = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val regexFindChatId = "\"chat\":\\{\"id\":(.+?),\"".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)

        val currentUpdateId: Int = regexFindUpdateId.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        updateId = currentUpdateId + 1

        val chatIdMatch = regexFindChatId.find(updates)
        val textMatch = messageTextRegex.find(updates)
        val chatId = chatIdMatch?.groups?.get(1)?.value?.toLongOrNull() ?: continue
        val text = textMatch?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        when {
            text?.lowercase() == "$OPEN_MENU" -> {
                botService.sendMenu(chatId)
            }

            data == STATISTICS_CLICKED -> {
                val statistics = trainer.getStatistics()
                val statisticsMessage =
                    "Выучено ${statistics.learnedWords} из ${statistics.totalCount} слов | ${statistics.percent}%"
                botService.sendMessage(chatId, statisticsMessage)
            }

            data == LEARN_WORDS_CLICKED -> {
                botService.checkNextQuestionAndSend(trainer, botService, chatId)
            }
        }
    }
}