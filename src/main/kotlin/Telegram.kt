package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val API = "https://api.telegram.org/bot"

class TelegramBotService(private val botToken: String) {
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$API$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, text: String): String {
        val urlSendMessage = "$API$botToken/sendMessage?chat_id=$chatId&text=$text"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}

fun main(args: Array<String>) {

    val botToken = args[0]
    val botService = TelegramBotService(botToken)
    var updateId = 0

    val regexFindUpdateId = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val regexFindChatId = "\"chat\":\\{\"id\":(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)

        val updateIdMatch = regexFindUpdateId.find(updates)

        if (updateIdMatch != null && updateIdMatch.groups.size > 1) {
            val currentUpdateId = regexFindUpdateId.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue

            updateId = currentUpdateId + 1
        }

        val chatIdMatch = regexFindChatId.find(updates)
        val textMatch = messageTextRegex.find(updates)

        val chatId = chatIdMatch?.groups?.get(1)?.value?.toLongOrNull() ?: continue
        val text = textMatch?.groups?.get(1)?.value

        if (text != null) {
            println("Получено сообщение: $text от chat_id: $chatId")
            botService.sendMessage(chatId, text)
        }
    }
}