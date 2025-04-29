package org.example

import org.example.dictionary_file.LearnWordsTrainer
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val API = "https://api.telegram.org/bot"
const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"

class TelegramBotService(private val botToken: String) {
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$API$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, text: String): String {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        println(encodedText)

        val urlSendMessage = "$API$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Long): String {
        val urlSendMessage = "$API$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "$LEARN_WORDS_CLICKED"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "$STATISTICS_CLICKED"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}

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

        if (text != null) {

            if (text.lowercase() == "hello") {
                botService.sendMessage(chatId, "Hello!")
            }
            if (text.lowercase() == "menu") {
                botService.sendMenu(chatId)
            }

            if (data != null) {

                when (data) {
                    STATISTICS_CLICKED -> {
                        val statistics = trainer.getStatistics()
                        val statisticsMessage =
                            "Выучено ${statistics.learnedWords} из ${statistics.totalCount} слов | ${statistics.percent}%"
                        botService.sendMessage(chatId, statisticsMessage)
                    }
                }
            }
        }
    }
}