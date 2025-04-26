package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0

    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val updateIdRegex = "\"update_id\"".toRegex()

    while (true) {
        Thread.sleep(2000)

        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val updateIdMatch = updateIdRegex.find(updates)
        val currentUpdateId = updateIdMatch?.groups?.get(1)?.value?.toIntOrNull()

        if (currentUpdateId != null) {
            updateId = currentUpdateId + 1
        }


        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val text = matchResult?.groups?.get(1)?.value

        if (text != null) {
            println("Получено сообщение: $text")
        }
    }
}

fun getUpdates(botToken: String, updateId: Int): String {

    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String>? = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response?.body().toString()
}