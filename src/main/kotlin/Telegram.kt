package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.dictionary_file.LearnWordsTrainer
import org.example.dictionary_file.TelegramBotService

const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val OPEN_MENU = "menu"
const val START_COMMAND = "/start"
const val RESET_STATISTICS_CLICKED = "reset_statistics_clicked"

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendmessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyBoard>>,
)

@Serializable
data class InlineKeyBoard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)


fun main(args: Array<String>) {

    val botToken = args[0]
    val botService = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()
    var lastUpdateId: Long = 0

    val json = Json {
        ignoreUnknownKeys = true
    }

    while (true) {
        Thread.sleep(2000)
        val responseString: String = botService.getUpdates(lastUpdateId)
        println(responseString)
        val response = json.decodeFromString<Response>(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1


        val message = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data


        when {
            message?.lowercase() == OPEN_MENU || message?.lowercase() == START_COMMAND -> {
                botService.sendMenu(json, chatId)
            }

            data == STATISTICS_CLICKED -> {
                val statistics = trainer.getStatistics()
                val statisticsMessage =
                    "Выучено ${statistics.learnedWords} из ${statistics.totalCount} слов | ${statistics.percent}%"
                botService.sendMessage(json, chatId, statisticsMessage)
            }

            data == LEARN_WORDS_CLICKED -> {
                botService.checkNextQuestionAndSend(json, trainer, botService, chatId)
            }

            data == "exit_menu" -> {
                botService.sendMenu(json, chatId)
            }

            data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                if (data == "exit_menu") {
                    botService.sendMenu(json, chatId)
                } else {
                    val userAnswerIndex = data.removePrefix(CALLBACK_DATA_ANSWER_PREFIX).toInt()
                    val isCorrect = trainer.checkAnswer(userAnswerIndex)

                    val correctAnswerWord = trainer.question?.correctAnswer?.originalWord
                    val correctTranslation = trainer.question?.correctAnswer?.translation

                    val responseMessage = if (isCorrect) {
                        "Правильно!"
                    } else {
                        "Неправильно! \"$correctAnswerWord\" – это \"$correctTranslation\"."
                    }

                    botService.sendMessage(json, chatId, responseMessage)
                    botService.checkNextQuestionAndSend(json, trainer, botService, chatId)
                }
            }

            data == RESET_STATISTICS_CLICKED -> {
                trainer.resetStatistics()
                botService.sendMessage(json, chatId, "Статистика сброшена.")
                botService.sendMenu(json, chatId)
            }
        }
    }
}