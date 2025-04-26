package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, text: String): String {
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}

fun main(args: Array<String>) {

    val botToken = args[0]
    val botService = TelegramBotService(botToken)
    var updateId = 0

    val updateIdRegex = "\"update_id\"".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex = "\"chat\":\\{\\\"id\\\":(\\d+)".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)

        val updateIdMatch = updateIdRegex.find(updates)

        if (updateIdMatch != null && updateIdMatch.groups.size > 1) {
            val currentUpdateId = updateIdMatch.groups[1]!!.value.toInt()
            updateId = currentUpdateId + 1
        }

        val chatIdMatch = chatIdRegex.find(updates)
        val textMatch = messageTextRegex.find(updates)

        val chatId = chatIdMatch?.groups?.get(1)?.value?.toLongOrNull()
        val text = textMatch?.groups?.get(1)?.value

        if (text != null && chatId != null) {
            println("Получено сообщение: $text от chat_id: $chatId")
            botService.sendMessage(chatId, text)
        }
    }
}