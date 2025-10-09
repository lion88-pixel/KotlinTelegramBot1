import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TelegramBotService(private val botToken: String) {

    private val httpClient = HttpClient.newBuilder().build()

    fun sendMessage(chatId: Long, text: String): String {
        if (text.isEmpty() || text.length > 4096) {
            return ""
        }
        val encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage" +
                "?chat_id=$chatId" +
                "&text=$encodedText"

        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun getUpdates(offset: Long): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$offset&timeout=60"
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    companion object {

        fun extractChatId(updateJson: String): Long? {
            val chatIdRegex = "\"chat\":\\{\"id\":(\\d+),".toRegex()
            val matchResult = chatIdRegex.find(updateJson)
            return matchResult?.groups?.get(1)?.value?.toLongOrNull()
        }

        fun extractMessageText(updateJson: String): String? {
            val messageTextRegex = "\"text\":\"(.*?)\"".toRegex()
            val matchResult = messageTextRegex.find(updateJson)
            return matchResult?.groups?.get(1)?.value
        }

        fun extractUpdateId(updateJson: String): Long? {
            val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
            val matchResult = updateIdRegex.find(updateJson)
            return matchResult?.groups?.get(1)?.value?.toLongOrNull()
        }
    }
}

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Ошибка: Токен бота не предоставлен.")
        return
    }

    val botToken = args[0]
    val botService = TelegramBotService(botToken)
    var updateId: Long = 0

    while (true) {
        Thread.sleep(2000)

        val updatesJson = botService.getUpdates(updateId)
        println("Получен JSON: $updatesJson")

        val updateIdList = mutableListOf<Long>()
        val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
        val updateIdMatches = updateIdRegex.findAll(updatesJson)

        updateIdMatches.forEach { match ->
            TelegramBotService.extractUpdateId(match.value)?.let { updateIdList.add(it) }
        }

        if (updateIdList.any())
            updateId = updateIdList.maxOrNull()!! + 1

        val chatId = TelegramBotService.extractChatId(updatesJson)
        val messageText = TelegramBotService.extractMessageText(updatesJson)

        if (chatId != null && messageText != null) {
            println("Получено сообщение '$messageText' от chat_id $chatId")
            val response = botService.sendMessage(chatId, messageText)
            println("Отправлен ответ: $response")

        } else {
            if (messageText == null)
                println("Не удалось извлечь текст сообщения")
            if (chatId == null)
                println("Не удалось извлечь chat_id")
        }
    }
}