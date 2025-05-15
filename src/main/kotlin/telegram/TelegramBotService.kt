package org.example.telegram

import kotlinx.serialization.json.Json
import org.example.dictionary_file.LearnWordsTrainer
import org.example.dictionary_file.Question
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.example.constants.*
import org.example.telegram.models.InlineKeyBoard
import org.example.telegram.models.ReplyMarkup
import org.example.telegram.models.SendmessageRequest

class TelegramBotService(private val botToken: String) {
    val json: Json = Json { ignoreUnknownKeys = true }
    private val client: HttpClient = HttpClient.newBuilder().build()

    companion object {
        const val API = "https://api.telegram.org/bot"
    }

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$API$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, message: String): String {
        val urlSendMessage = "$API$botToken/sendMessage"
        val requestBody = SendmessageRequest(
            chatId = chatId,
            text = message,
        )

        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Long): String {
        val urlSendMessage = "$API$botToken/sendMessage"
        val requestBody = SendmessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyBoard(text = "Изучать слова", callbackData = LEARN_WORDS_CLICKED),
                        InlineKeyBoard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                    ),
                    listOf(
                        InlineKeyBoard(text = "Сброс статистики", callbackData = RESET_STATISTICS_CLICKED)
                    )
                )
            )
        )

        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestion(chatId: Long, question: Question) {

        val text = question.correctAnswer.originalWord

        val replyMarkup = ReplyMarkup(
            listOf(
                question.variants.mapIndexed { index, word ->
                    InlineKeyBoard(text = word.translation, callbackData = "${CALLBACK_DATA_ANSWER_PREFIX}$index")
                }.map { listOf(it) } + listOf(
                    listOf(InlineKeyBoard(text = "Выйти в меню", callbackData = EXIT_MENU_CLICKED))
                )
            ).flatten()
        )

        val requestBody = SendmessageRequest(
            chatId = chatId,
            text = text,
            replyMarkup = replyMarkup
        )

        val requestBodyString = json.encodeToString(requestBody)

        val urlSendMessage = "$API$botToken/sendMessage"
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            println("Ошибка при отправке вопроса: ${response.body()}")
        }
    }

    fun checkNextQuestionAndSend(
        trainer: LearnWordsTrainer,
        chatId: Long,
    ) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(chatId, "Все слова в словаре выучены.")
        } else {
            sendQuestion(chatId, question)
        }
    }
}