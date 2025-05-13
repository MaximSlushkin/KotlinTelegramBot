package org.example

import org.example.dictionary_file.LearnWordsTrainer
import org.example.telegram.TelegramBotService
import org.example.constants.*
import org.example.telegram.models.Response
import org.example.telegram.models.Update

fun main(args: Array<String>) {

    val botToken = args[0]

    val trainers = HashMap<Long, LearnWordsTrainer>()
    val botService = TelegramBotService(botToken)
    var lastUpdateId: Long = 0

    while (true) {
        Thread.sleep(2000)
        val responseString: String = botService.getUpdates(lastUpdateId)
        println(responseString)

        val response = botService.json.decodeFromString<Response>(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdates(it, botService, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdates(
    update: Update,
    botService: TelegramBotService,
    trainers: HashMap<Long, LearnWordsTrainer>
) {
    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val currentTrainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    when {
        message?.lowercase() == OPEN_MENU || message?.lowercase() == START_COMMAND -> {
            botService.sendMenu(chatId)
        }

        data == STATISTICS_CLICKED -> {
            val statistics = currentTrainer.getStatistics()
            val statisticsMessage =
                "Выучено ${statistics.learnedWords} из ${statistics.totalCount} слов | ${statistics.percent}%"
            botService.sendMessage(chatId, statisticsMessage)
        }

        data == LEARN_WORDS_CLICKED -> {
            botService.checkNextQuestionAndSend(currentTrainer, chatId)
        }

        data == EXIT_MENU_CLICKED -> {
            botService.sendMenu(chatId)
        }

        data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {

            val userAnswerIndex = data.removePrefix(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            val isCorrect = currentTrainer.checkAnswer(userAnswerIndex)

            val responseMessage = if (isCorrect) {
                "Правильно!"
            } else {
                val correctAnswerWord = currentTrainer.question?.correctAnswer?.originalWord
                val correctTranslation = currentTrainer.question?.correctAnswer?.translation

                "Неправильно! \"$correctAnswerWord\" – это \"$correctTranslation\"."
            }

            botService.sendMessage(chatId, responseMessage)
            botService.checkNextQuestionAndSend(currentTrainer, chatId)
        }

        data == RESET_STATISTICS_CLICKED -> {
            currentTrainer.resetStatistics()
            botService.sendMessage(chatId, "Статистика сброшена. Выход в основное меню")
            botService.sendMenu(chatId)
        }
    }
}