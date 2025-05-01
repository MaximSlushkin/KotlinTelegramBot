package org.example.dictionary_file

import org.example.CALLBACK_DATA_ANSWER_PREFIX
import org.example.LEARN_WORDS_CLICKED
import org.example.STATISTICS_CLICKED
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {
    private val client: HttpClient = HttpClient.newBuilder().build()

    companion object {
        const val API = "https://api.telegram.org/bot"
    }

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

    fun sendQuestion(chatId: Long, question: Question,) {
        val text = question.correctAnswer.originalWord
        val inlineKeyboard = question.variants.mapIndexed { index, word ->
            """
            {
                "text": "${word.translation}",
                "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX$index"
            }
        """.trimIndent()
        }

        val keyboardJson = """
        [
            ${inlineKeyboard.joinToString(",")}
        ]
    """.trimIndent()

        val body = """
        {
            "chat_id": $chatId,
            "text": "$text",
            "reply_markup": {
                "inline_keyboard": $keyboardJson
            }
        }
    """.trimIndent()
        val urlSendMessage = "$API$botToken/sendMessage"
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun checkNextQuestionAndSend(
        trainer: LearnWordsTrainer,
        botService: TelegramBotService,
        chatId: Long,
    ) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            botService.sendMessage(chatId, "Все слова в словаре выучены.")
        } else {
            sendQuestion(chatId, question)
        }
    }
}